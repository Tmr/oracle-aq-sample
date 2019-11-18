package com.mkb.aqtransfer;

import nl.chess.it.util.config.Config;
import nl.chess.it.util.config.ConfigValidationResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

public class ClientConfig extends Config {
    /**
     * Name of the file we are looking for to read the configuration.
     */
    private static final String FILE_NAME = "config/config.properties";
    private static final String SECOND_FILE_NAME = "../config/config.properties";

    private ClientConfig(Properties props) {
        super(props);
    }

    public String getFromHostName() {
        return getString("client.from.hostname");
    }

    public String getFromSID() {
        return getString("client.from.sid");
    }

    public int getFromPort() {
        return getInt("client.from.port");
    }

    public String getFromQueueName() {
        return getString("client.from.queue_name");
    }

    public String getToHostName() {
        return getString("client.to.hostname");
    }

    public String getToSID() {
        return getString("client.to.sid");
    }

    public int getToPort() {
        return getInt("client.to.port");
    }

    public String getToQueueName() {
        return getString("client.to.queue_name");
    }

    private static ClientConfig config;

    private static Properties getProperties() throws IOException {
        File configFile = new File(FILE_NAME);
        if (!configFile.exists())
            configFile = new File(SECOND_FILE_NAME);
        System.out.println("Reading config from " + configFile.getAbsolutePath());

        FileInputStream fis = null;
        Properties properties = new Properties();

        try {
            fis = new FileInputStream(configFile);
            properties.load(fis);
            return properties;
        } finally {
            if(fis != null) {
                fis.close();
            }
        }
    }

    public static ClientConfig getConfig() throws IOException {
        if (config == null) {
            config = new ClientConfig(getProperties());

            ConfigValidationResult configResult = config.validateConfiguration();

            if (configResult.thereAreErrors()) {
                System.out.println("Errors in configuration");

                for (Iterator iter = configResult.getErrors().iterator(); iter.hasNext(); ) {
                    System.out.println(" > " + iter.next());
                }

                System.exit(1);
            }

            if (configResult.thereAreUnusedProperties()) {
                System.out.println("Unused properties");

                for (Iterator iter = configResult.getUnusedProperties().iterator(); iter.hasNext(); ) {
                    System.out.println(" > " + iter.next());
                }
            }
        }

        return config;
    }

}
