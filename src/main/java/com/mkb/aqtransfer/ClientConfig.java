package com.mkb.aqtransfer;

import nl.chess.it.util.config.Config;
import nl.chess.it.util.config.ConfigValidationResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

class ClientConfig extends Config {
    /**
     * Name of the file we are looking for to read the configuration.
     */
    private static final String FILE_NAME = "config/config.properties";
    private static final String SECOND_FILE_NAME = "../config/config.properties";

    private ClientConfig(Properties props) {
        super(props);
    }

    String getFromHostName() {
        return getString("client.from.hostname");
    }

    String getFromSID() {
        return getString("client.from.sid");
    }

    int getFromPort() {
        return getInt("client.from.port");
    }

    String getFromQueueName() {
        return getString("client.from.queue_name");
    }

    String getFromQueueOwner() {
        return getString("client.from.queue_owner");
    }

    String getFromUser() {
        return getString("client.from.user");
    }

    String getFromPassword() {
        return getString("client.from.password");
    }

    String getToHostName() {
        return getString("client.to.hostname");
    }

    String getToSID() {
        return getString("client.to.sid");
    }

    int getToPort() {
        return getInt("client.to.port");
    }

    String getToQueueName() {
        return getString("client.to.queue_name");
    }

    String getToQueueOwner() {
        return getString("client.to.queue_owner");
    }

    String getToUser() {
        return getString("client.to.user");
    }

    String getToPassword() {
        return getString("client.to.password");
    }

    private static ClientConfig config;

    private static Properties getProperties() throws IOException {
        File configFile = new File(FILE_NAME);
        if (!configFile.exists())
            configFile = new File(SECOND_FILE_NAME);
        System.out.println("Reading config from " + configFile.getAbsolutePath());

        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
            return properties;
        }
    }

    static ClientConfig getConfig() throws IOException {
        if (config == null) {
            config = new ClientConfig(getProperties());

            ConfigValidationResult configResult = config.validateConfiguration();

            if (configResult.thereAreErrors()) {
                System.out.println("Errors in configuration");

                for (Object o : configResult.getErrors()) {
                    System.out.println(" > " + o);
                }

                System.exit(1);
            }

            if (configResult.thereAreUnusedProperties()) {
                System.out.println("Unused properties");

                for (Object o : configResult.getUnusedProperties()) {
                    System.out.println(" > " + o);
                }
            }
        }

        return config;
    }

}
