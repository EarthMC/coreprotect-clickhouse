package net.coreprotect.utility;

import net.coreprotect.CoreProtect;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public record GitProperties(String commit, String commitShort, String branch, String repositoryUrl, String message) {
    public static GitProperties retrieve(CoreProtect plugin) throws IOException {
        try (final InputStream is = plugin.getResource("git.properties")) {
            if (is == null) {
                throw new IOException("Could not find version.properties in the Towny jar file.");
            }

            final Properties properties = new Properties();
            try (final InputStreamReader reader = new InputStreamReader(is)) {
                properties.load(reader);
            }

            return new GitProperties(
                    properties.getProperty("git.commit.id"),
                    properties.getProperty("git.commit.id.abbrev"),
                    properties.getProperty("git.branch"),
                    properties.getProperty("git.remote.origin.url"),
                    properties.getProperty("git.commit.message.short")
            );
        }
    }
}
