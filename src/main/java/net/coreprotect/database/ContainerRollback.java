package net.coreprotect.database;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.coreprotect.data.lookup.LookupResult;
import net.coreprotect.data.lookup.result.CommonLookupResult;
import net.coreprotect.data.lookup.type.CommonLookupData;
import net.coreprotect.utility.serialize.SerializedItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.consumer.process.Process;
import net.coreprotect.database.rollback.Rollback;
import net.coreprotect.database.rollback.RollbackComplete;
import net.coreprotect.language.Phrase;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.MaterialUtils;

public class ContainerRollback extends Rollback {

    public static void performContainerRollbackRestore(Statement statement, CommandSender user, List<String> checkUuids, List<String> checkUsers, String timeString, List<Object> restrictList, Map<Object, Boolean> excludeList, List<String> excludeUserList, List<Integer> actionList, final Location location, Integer[] radius, long startTime, long endTime, boolean restrictWorld, boolean lookup, boolean verbose, final int rollbackType) {
        try {
            long timeStart = System.currentTimeMillis();

            final LookupResult<?> rawLookupResult = Lookup.performLookup(statement, user, checkUuids, checkUsers, restrictList, excludeList, excludeUserList, actionList, location, radius, null, startTime, endTime, -1, -1, restrictWorld, lookup, false);
            if (!(rawLookupResult instanceof CommonLookupResult lookupResult)) {
                return;
            }

            if (rollbackType == 1) {
                Collections.reverse(rawLookupResult.data());
            }

            String userString = "#server";
            if (user != null) {
                userString = user.getName();
            }

            Queue.queueRollbackUpdate(userString, lookupResult.data(), Process.CONTAINER_ROLLBACK_UPDATE, rollbackType); // Perform update transaction in consumer

            final String finalUserString = userString;
            ConfigHandler.rollbackHash.put(userString, new int[] { 0, 0, 0, 0, 0 });

            Scheduler.scheduleSyncDelayedTask(CoreProtect.getInstance(), () -> {
                try {
                    int[] rollbackHashData = ConfigHandler.rollbackHash.get(finalUserString);
                    int itemCount = rollbackHashData[0];
                    // int blockCount = rollbackHashData[1];
                    int entityCount = rollbackHashData[2];
                    Block block = location.getBlock();

                    if (!block.getWorld().isChunkLoaded(block.getChunk())) {
                        block.getWorld().getChunkAt(block.getLocation());
                    }
                    Object container = null;
                    Material type = block.getType();
                    List<ItemFrame> matchingFrames = new ArrayList<>();

                    if (BlockGroup.CONTAINERS.contains(type)) {
                        container = BlockUtils.getContainerInventory(block.getState(), false);
                    }
                    else {
                        for (Entity entity : block.getChunk().getEntities()) {
                            if (entity.getLocation().getBlockX() == location.getBlockX() && entity.getLocation().getBlockY() == location.getBlockY() && entity.getLocation().getBlockZ() == location.getBlockZ()) {
                                if (entity instanceof ArmorStand) {
                                    type = Material.ARMOR_STAND;
                                    container = ItemUtils.getEntityEquipment((LivingEntity) entity);
                                }
                                else if (entity instanceof ItemFrame) {
                                    type = Material.ITEM_FRAME;
                                    container = entity;
                                    matchingFrames.add((ItemFrame) entity);
                                }
                            }
                        }
                    }

                    int modifyCount = 0;
                    if (container != null) {
                        for (final CommonLookupData row : lookupResult.data()) {
                            // int unixtimestamp = (int) (System.currentTimeMillis() / 1000L);
                            // int rowId = lookupRow[0];
                            // int rowTime = (Integer)lookupRow[1];
                            // int rowUserId = (Integer)lookupRow[2];
                            // int rowX = (Integer)lookupRow[3];
                            // int rowY = (Integer)lookupRow[4];
                            // int rowZ = (Integer)lookupRow[5];
                            int rowTypeRaw = row.type();
                            // int rowData = row.data();
                            int rowAction = row.action();
                            int rowRolledBack = MaterialUtils.rolledBack(row.rolledBack(), false);
                            // int rowWid = (Integer)lookupRow[10];
                            int rowAmount = row.amount();
                            String rowMetadata = row.metadata();
                            Material rowType = MaterialUtils.getType(rowTypeRaw);

                            if ((rollbackType == 0 && rowRolledBack == 0) || (rollbackType == 1 && rowRolledBack == 1)) {
                                modifyCount = modifyCount + rowAmount;
                                int action = 0;

                                if (rollbackType == 0 && rowAction == 0) {
                                    action = 1;
                                }

                                if (rollbackType == 1 && rowAction == 1) {
                                    action = 1;
                                }

                                SerializedItem populatedStack = ItemUtils.deserializeItem(rowMetadata, rowType, rowAmount);
                                Integer slot = populatedStack.slot();
                                BlockFace faceData = populatedStack.faceData();
                                ItemStack itemstack = populatedStack.itemStack();

                                if (type == Material.ITEM_FRAME && faceData != null) {
                                    ItemFrame itemFrame = (ItemFrame) container;
                                    if (faceData != itemFrame.getFacing()) {
                                        for (ItemFrame frame : matchingFrames) {
                                            if (faceData == frame.getFacing()) {
                                                container = frame;
                                                break;
                                            }
                                        }
                                    }
                                }

                                Rollback.modifyContainerItems(type, container, slot != null ? slot : 0, itemstack, action);
                            }
                        }
                    }
                    matchingFrames.clear();

                    ConfigHandler.rollbackHash.put(finalUserString, new int[] { itemCount, modifyCount, entityCount, 1, 1 });
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }, location, 0);

            int[] rollbackHashData = ConfigHandler.rollbackHash.get(finalUserString);
            int next = rollbackHashData[3];
            int sleepTime = 0;

            while (next == 0) {
                sleepTime = sleepTime + 5;
                Thread.sleep(5);
                rollbackHashData = ConfigHandler.rollbackHash.get(finalUserString);
                next = rollbackHashData[3];
                if (sleepTime > 300000) {
                    Chat.console(Phrase.build(Phrase.ROLLBACK_ABORTED));
                    break;
                }
            }

            rollbackHashData = ConfigHandler.rollbackHash.get(finalUserString);
            int blockCount = rollbackHashData[1];
            long timeFinish = System.currentTimeMillis();
            double totalSeconds = (timeFinish - timeStart) / 1000.0;

            if (user != null) {
                int file = -1;
                if (blockCount > 0) {
                    file = 1;
                }
                int itemCount = 0;
                int entityCount = 0;

                RollbackComplete.output(user, location, checkUsers, restrictList, excludeList, excludeUserList, actionList, timeString, file, totalSeconds, itemCount, blockCount, entityCount, rollbackType, radius, verbose, restrictWorld, 0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
