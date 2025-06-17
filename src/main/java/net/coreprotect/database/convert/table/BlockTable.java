package net.coreprotect.database.convert.table;

import net.coreprotect.database.convert.TableData;
import net.coreprotect.database.convert.TableMapping;
import net.coreprotect.database.convert.process.BlockConverter;
import net.coreprotect.database.convert.process.ConvertProcess;

public class BlockTable extends TableData {
    @Override
    public String getName() {
        return "block";
    }

    @Override
    public TableMapping getColumnMapping() {
        return TableMapping.map().columns("rowid", "time", "user", "wid", "x", "y", "z", "type", "data", "meta", "blockdata", "action", "rolled_back").finish();
    }

    @Override
    public ConvertProcess converter() {
        return new BlockConverter(this);
    }
}
