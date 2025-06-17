package net.coreprotect.database.convert.table;

import net.coreprotect.database.convert.TableData;
import net.coreprotect.database.convert.TableMapping;
import net.coreprotect.database.convert.process.ConvertProcess;
import net.coreprotect.database.convert.process.EntityConverter;

public class EntityTable extends TableData {
    @Override
    public String getName() {
        return "entity";
    }

    @Override
    public TableMapping getColumnMapping() {
        return TableMapping.map().columns("rowid", "time", "data").finish();
    }

    @Override
    public ConvertProcess converter() {
        return new EntityConverter(this);
    }
}
