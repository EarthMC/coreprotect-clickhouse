package net.coreprotect.database.convert;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

public class TableMapping {
    private LinkedHashMap<String, String> mapping = new LinkedHashMap<>();

    public TableMapping(SequencedMap<String, String> map) {
        this.mapping.putAll(map);
    }

    public LinkedHashMap<String, String> getMapping() {
        return mapping;
    }

    public static Builder map() {
        return new Builder();
    }

    public static class Builder {
        private LinkedHashMap<String, String> mapping = new LinkedHashMap<>();

        public Builder convert(String from, String to) {
            this.mapping.put(from, to);
            return this;
        }

        public Builder column(String column) {
            this.mapping.put(column, column);
            return this;
        }

        public Builder columns(String... columns) {
            for (final String column : columns) {
                column(column);
            }

            return this;
        }

        public TableMapping finish() {
            return new TableMapping(this.mapping);
        }
    }
}
