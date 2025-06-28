package net.coreprotect.database.convert.process;

import net.coreprotect.database.convert.ClickhouseConverter;
import net.coreprotect.database.convert.table.ItemTable;
import net.coreprotect.database.rollback.RollbackUtil;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.serialize.Bytes;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemConverter implements ConvertProcess {
    private final ItemTable table;

    public ItemConverter(ItemTable table) {
        this.table = table;
    }

    @Override
    public void convertTable(ClickhouseConverter converter, ConvertOptions options, Connection connection) throws SQLException {
        long batchCount = 0;

        try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + table.fullName() + " (rowid, time, user, wid, x, y, z, type, data, amount, action, rolled_back) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement readStatement = connection.prepareStatement("SELECT rowid, time, user, wid, x, y, z, type, hex(data), amount, action, rolled_back FROM " + converter.formatMysqlSource(table) + " OFFSET " + options.offset())) {

            final ResultSet rs = readStatement.executeQuery();
            while (converter.next(rs, insertStatement, batchCount)) {
                final int rowId = rs.getInt(1);
                insertStatement.setInt(1, rowId); // rowid
                insertStatement.setInt(2, rs.getInt(2)); // time
                insertStatement.setInt(3, rs.getInt(3)); // user
                insertStatement.setInt(4, rs.getInt(4)); // wid
                insertStatement.setInt(5, rs.getInt(5)); // x
                insertStatement.setInt(6, rs.getInt(6)); // y
                insertStatement.setInt(7, rs.getInt(7)); // z
                insertStatement.setInt(8, rs.getInt(8)); // type

                String jsonData = convertItemHexToJson(converter, rowId, rs.getString(9), rs.getInt(8), rs.getInt(10));

                insertStatement.setString(9, jsonData); // data
                insertStatement.setInt(10, rs.getInt(10)); // amount
                insertStatement.setInt(11, rs.getInt(11)); // action
                insertStatement.setInt(12, rs.getInt(12)); // rolled_back

                insertStatement.addBatch();

                if (++batchCount % 50_000 == 0) {
                    insertStatement.executeBatch();
                }
            }

            insertStatement.executeBatch();
        }
    }

    public static String convertItemHexToJson(ClickhouseConverter converter, int rowId, String hexData, int type, int amount) {
        try {
            final byte[] bytes = Bytes.fromHexString(hexData);
            if (bytes == null) {
                return null;
            }

            final Material material = MaterialUtils.getType(type);
            if (!material.isItem()) {
                converter.logger().warn("Could not convert item at row id {} because material with id {} is not an item (got {})", rowId, type, material.getKey().asMinimalString());
                return null;
            }

            Object[] deserialized = RollbackUtil.populateItemStack(ItemStack.of(material, Math.min(amount, 99)), bytes);

            int slot = (int) deserialized[0];
            BlockFace face = ItemUtils.parseBlockFaceOrNull((String) deserialized[1]);
            ItemStack itemStack = (ItemStack) deserialized[2];

            if (face == null && !ItemUtils.hasNonTrivialData(itemStack)) {
                return null;
            }

            return ItemUtils.serializeItem(itemStack, slot, face);
        } catch (Exception e) {
            converter.logger().warn("Failed to convert item at row id {} to json", rowId, e);
            return null;
        }
    }
}
