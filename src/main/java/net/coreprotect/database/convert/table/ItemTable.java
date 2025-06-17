package net.coreprotect.database.convert.table;

import net.coreprotect.database.convert.TableData;
import net.coreprotect.database.convert.TableMapping;
import net.coreprotect.database.convert.process.ConvertProcess;
import net.coreprotect.database.convert.process.ItemConverter;

public class ItemTable extends TableData {
    @Override
    public String getName() {
        return "item";
    }

    @Override
    public TableMapping getColumnMapping() {
        return TableMapping.map()
                .columns("rowid", "time", "user", "wid", "x", "y", "z", "type")
                .convert("data", "hex(data) as data")
                .columns("amount", "action", "rolled_back").finish();
    }

    @Override
    public ConvertProcess converter() {
        return new ItemConverter(this);
    }
}
