package net.coreprotect.database.convert.table;

import net.coreprotect.database.convert.TableData;
import net.coreprotect.database.convert.TableMapping;
import net.coreprotect.database.convert.process.ConvertProcess;
import net.coreprotect.database.convert.process.SimpleConvertProcess;

public class MaterialMapTable extends TableData {
    @Override
    public String getName() {
        return "material_map";
    }

    @Override
    public TableMapping getColumnMapping() {
        return TableMapping.map().columns("rowid", "id", "material").finish();
    }

    @Override
    public ConvertProcess converter() {
        return new SimpleConvertProcess(this);
    }
}
