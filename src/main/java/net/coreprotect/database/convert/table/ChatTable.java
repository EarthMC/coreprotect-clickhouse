package net.coreprotect.database.convert.table;

import net.coreprotect.database.convert.TableData;
import net.coreprotect.database.convert.TableMapping;
import net.coreprotect.database.convert.process.ConvertProcess;
import net.coreprotect.database.convert.process.SimpleConvertProcess;

public class ChatTable extends TableData {
    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public TableMapping getColumnMapping() {
        return TableMapping.map()
                .column("rowid")
                .column("time")
                .column("user")
                .column("wid")
                .columns("x", "y", "z")
                .column("message")
                .finish();
    }

    @Override
    public ConvertProcess converter() {
        return new SimpleConvertProcess(this);
    }
}
