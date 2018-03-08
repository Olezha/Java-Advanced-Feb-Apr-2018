package com.flowergarden.storage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class JdbcConnectionPool implements AutoCloseable {

    private List<JdbcConnectionFromPool> connectionsPool = new ArrayList<>();
    private String datasourceUrl;
    private int connectionPoolSize = 10;

    public JdbcConnectionPool() throws SQLException {
        loadProperties();

        for (int i = 0; i < connectionPoolSize; i++)
            connectionsPool.add(
                    new JdbcConnectionFromPool(
                            DriverManager.getConnection(datasourceUrl)));
    }

    public Connection getConnection() throws SQLException {
        for (JdbcConnectionFromPool connection : connectionsPool) {
            if (!connection.isBusy()) {
                connection.setBusy();
                return connection;
            }
        }

        // TODO: implement a queue
        return null;
    }

    @Override
    public void close() throws Exception {
        for (JdbcConnectionFromPool connection : connectionsPool) {
            connection.closeConnection();
        }
    }

    private void loadProperties() {
        try (InputStream propertiesStream =
                     Thread.currentThread().getContextClassLoader()
                             .getResourceAsStream("application.properties")) {
            Properties properties = new Properties();
            properties.load(propertiesStream);

            if (properties.containsKey("datasource.url"))
                datasourceUrl = properties.getProperty("datasource.url");
            if (properties.containsKey("connection.pool.size")) {
                try {
                    connectionPoolSize = Integer.parseInt(properties.getProperty("connection.pool.size"));
                } catch (NumberFormatException e) {
                    System.out.println("Parameter \"connection.pool.size\" has a wrong format");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
