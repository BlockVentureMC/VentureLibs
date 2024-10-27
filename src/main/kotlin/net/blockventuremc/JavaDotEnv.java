package net.blockventuremc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class JavaDotEnv {

    private final File envFile = new File(".env");

    private final Set<DotenvEntry> entries = new HashSet<>();

    public JavaDotEnv() {
        if (!envFile.exists()) {
            System.out.println("No .env file found, creating one...");
            try {
                envFile.createNewFile();
            } catch (Exception e) {
                System.out.println("Failed to create .env file: " + e.getMessage());
            }
        }

        // Load the entries
        loadEntries();
    }

    private void loadEntries() {
        // Load the entries from the file

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(envFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                final var split = line.split("=", 2);
                if (split.length != 2) {
                    System.out.println("Invalid dotenv entry: " + line);
                    continue;
                }

                entries.add(new DotenvEntry(split[0], split[1]));
            }
        } catch (Exception e) {
            System.out.println("Failed to load .env file: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    System.out.println("Failed to close reader: " + e.getMessage());
                }
            }
        }
    }

    public String get(String key) {
        for (DotenvEntry entry : entries) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }


    /**
     * A key value pair representing an environment variable and its value
     */
    private class DotenvEntry {

        private final String key;
        private final String value;

        /**
         * Creates a new dotenv entry using the provided key and value
         *
         * @param key   the dotenv entry name
         * @param value the dotenv entry value
         */
        public DotenvEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the key for the {@link DotenvEntry}
         *
         * @return the key for the {@link DotenvEntry}
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns the value for the {@link DotenvEntry}
         *
         * @return the value for the {@link DotenvEntry}
         */
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

}
