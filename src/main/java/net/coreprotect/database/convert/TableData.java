package net.coreprotect.database.convert;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.convert.process.ConvertProcess;

public abstract class TableData {
    public abstract String getName();

    public String fullName() {
        return ConfigHandler.prefix + this.getName();
    }

    public abstract TableMapping getColumnMapping();

    public abstract ConvertProcess converter();
}
