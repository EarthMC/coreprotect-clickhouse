package net.coreprotect.database.convert.table;

import net.coreprotect.database.convert.TableData;
import net.coreprotect.database.convert.TableMapping;
import net.coreprotect.database.convert.process.ConvertProcess;
import net.coreprotect.database.convert.process.SimpleConvertProcess;

public class SignTable extends TableData {
    @Override
    public String getName() {
        return "sign";
    }

    @Override
    public TableMapping getColumnMapping() {
        return TableMapping.map().columns("rowid", "time", "user", "wid", "x", "y", "z", "action", "color", "color_secondary", "data", "waxed", "face",
                "line_1", "line_2", "line_3", "line_4", "line_5", "line_6", "line_7", "line_8").finish();
    }

    @Override
    public ConvertProcess converter() {
        return new SimpleConvertProcess(this);
    }
}
