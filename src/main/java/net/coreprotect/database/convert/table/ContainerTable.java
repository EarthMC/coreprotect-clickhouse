package net.coreprotect.database.convert.table;

import net.coreprotect.database.convert.TableData;
import net.coreprotect.database.convert.TableMapping;
import net.coreprotect.database.convert.process.ContainerConverter;
import net.coreprotect.database.convert.process.ConvertProcess;

public class ContainerTable extends TableData {
    @Override
    public String getName() {
        return "container";
    }

    @Override
    public TableMapping getColumnMapping() {
        return TableMapping.map().columns("rowid", "time", "user", "wid", "x", "y", "z", "type", "data", "amount", "metadata", "action", "rolled_back").finish();
    }

    @Override
    public ConvertProcess converter() {
        return new ContainerConverter(this);
    }
}
