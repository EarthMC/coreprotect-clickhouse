package net.coreprotect.database.convert.process;

import net.coreprotect.database.convert.ClickhouseConverter;
import net.coreprotect.database.convert.table.BlockTable;
import net.coreprotect.database.rollback.RollbackUtil;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.serialize.BannerData;
import net.coreprotect.utility.serialize.Bytes;
import net.coreprotect.utility.serialize.JsonSerialization;
import net.coreprotect.utility.serialize.SerializedBlockMeta;
import net.coreprotect.utility.serialize.SerializedItem;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BlockConverter implements ConvertProcess {
    private final BlockTable table;

    public BlockConverter(BlockTable table) {
        this.table = table;
    }

    @Override
    public void convertTable(ClickhouseConverter converter, ConvertOptions options, Connection connection) throws SQLException {
        long batchCount = 0;

        try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + table.fullName() + " (rowid, time, user, wid, x, y, z, type, data, meta, blockdata, action, rolled_back) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement readStatement = connection.prepareStatement("SELECT rowid, time, user, wid, x, y, z, type, data, hex(meta), toString(blockdata), action, rolled_back FROM " + converter.formatMysqlSource(table) + " OFFSET " + options.offset())) {

            ResultSet rs = readStatement.executeQuery();
            while (rs.next()) {
                insertStatement.setLong(1, rs.getLong(1)); // rowid
                insertStatement.setInt(2, rs.getInt(2)); // time
                insertStatement.setInt(3, rs.getInt(3)); // user
                insertStatement.setInt(4, rs.getInt(4)); // wid
                insertStatement.setInt(5, rs.getInt(5)); // x
                insertStatement.setInt(6, rs.getInt(6)); // y
                insertStatement.setInt(7, rs.getInt(7)); // z

                final int rawBlockType = rs.getInt(8);
                insertStatement.setInt(8, rawBlockType); // type
                insertStatement.setInt(9, rs.getInt(9)); // data

                // convert block meta to json
                String meta = null;
                byte[] data = Bytes.fromHexString(rs.getString(10));
                if (data != null) {
                    final Material type = MaterialUtils.getType(rawBlockType);

                    final List<Object> rawMeta = RollbackUtil.deserializeMetadata(data);

                    String command = null;
                    Collection<SerializedItem> items = null;
                    BannerData bannerData = null;

                    if (Tag.SHULKER_BOXES.isTagged(type)) {
                        for (final Object object : rawMeta) {
                            final ItemStack itemStack = ItemUtils.unserializeItemStackLegacy(object);
                            if (itemStack != null) {
                                if (items == null) {
                                    items = new ArrayList<>();
                                }

                                items.add(SerializedItem.of(itemStack));
                            }
                        }
                    } else if (type == Material.COMMAND_BLOCK || type == Material.CHAIN_COMMAND_BLOCK || type == Material.REPEATING_COMMAND_BLOCK) {
                        for (final Object object : rawMeta) {
                            if (object instanceof String string) {
                                command = string;
                                break;
                            }
                        }
                    } else if (Tag.BANNERS.isTagged(type)) {
                        DyeColor baseColor = null;
                        List<Pattern> patterns = new ArrayList<>();

                        for (final Object value : rawMeta) {
                            if (value instanceof DyeColor dyeColor) {
                                baseColor = dyeColor;
                            }
                            else if (value instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Pattern pattern = new Pattern((Map<String, Object>) value);
                                patterns.add(pattern);
                            }
                        }

                        if (baseColor != null) {
                            bannerData = new BannerData(baseColor, patterns);
                        }
                    }

                    if (command != null || items != null || bannerData != null) {
                        meta = JsonSerialization.GSON.toJson(new SerializedBlockMeta(command, items, bannerData));
                    }
                }

                insertStatement.setString(10, meta);
                insertStatement.setString(11, rs.getString(11)); // blockdata
                insertStatement.setInt(12, rs.getInt(12)); // action
                insertStatement.setBoolean(13, rs.getBoolean(13)); // rolled_back

                insertStatement.addBatch();

                if (++batchCount % 100_000 == 0) {
                    insertStatement.executeBatch();
                }
            }

            insertStatement.executeBatch();
        }
    }
}
