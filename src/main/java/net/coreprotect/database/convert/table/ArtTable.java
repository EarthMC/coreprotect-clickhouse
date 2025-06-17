package net.coreprotect.database.convert.table;

import net.coreprotect.database.convert.TableData;
import net.coreprotect.database.convert.TableMapping;
import net.coreprotect.database.convert.process.ConvertProcess;
import net.coreprotect.database.convert.process.SimpleConvertProcess;

public class ArtTable extends TableData {
    @Override
    public String getName() {
        return "art_map";
    }

    @Override
    public TableMapping getColumnMapping() {
        return TableMapping.map().columns("rowid", "id", "art").finish();
    }

    @Override
    public ConvertProcess converter() {
        return new SimpleConvertProcess(this);
    }
}
