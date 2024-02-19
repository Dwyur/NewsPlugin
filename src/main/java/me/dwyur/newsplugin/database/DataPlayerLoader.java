package me.dwyur.newsplugin.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.dwyur.newsplugin.database.player.DataPlayer;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
public class DataPlayerLoader {
    final String host, password, user, data;
    final int port;
    final HikariDataSource ds;
    Connection connection = null;
    final ExecutorService executor = Executors.newFixedThreadPool(5);

    public DataPlayerLoader(String host, String password, String user, String data, int port) {
        this.host = host;
        this.password = password;
        this.user = user;
        this.data = data;
        this.port = port;
        this.ds = getDataSource();
        refreshConnection();
        initTables();
    }

    public CompletableFuture<DataPlayer> getDataPlayer(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            String selectQuery = "SELECT id, latest_post_id FROM news_plugin WHERE player_name = ?";
            String insertQuery = "INSERT INTO news_plugin (player_name, latest_post_id) VALUES (?, ?)";

            try (Connection connection = getConnection();
                 PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

                selectStatement.setString(1, playerName);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        insertStatement.setString(1, playerName);
                        insertStatement.setInt(2, 0);
                        insertStatement.executeUpdate();
                        return new DataPlayer(-1, playerName, 0);
                    } else {
                        int id = resultSet.getInt("id");
                        long latest_post_id = resultSet.getLong("latest_post_id");
                        return new DataPlayer(id, playerName, latest_post_id);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }



    public void setLastPostId(DataPlayer dataPlayer, long postId) {
        executor.execute(() -> {
            String query = "UPDATE news_plugin SET latest_post_id = ? WHERE player_name = ?";
            try (PreparedStatement statement = getConnection().prepareStatement(query)) {
                statement.setLong(1, postId);
                statement.setString(2, dataPlayer.getName());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }



    void initTables() {
        CompletableFuture.runAsync(() -> {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS news_plugin (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "`player_name` VARCHAR(16) NOT NULL," +
                    "`latest_post_id` BIGINT NOT NULL)";
            try (Statement statement = getConnection().createStatement()) {
                statement.execute(createTableQuery);
            } catch (SQLException e) {
                throw new RuntimeException();
            }
        });
    }

    Connection getConnection() {
        refreshConnection();
        return connection;
    }

    @SneakyThrows
    protected void refreshConnection() {
        if (connection != null && !connection.isClosed() && connection.isValid(1000)) {
            return;
        }

        this.connection = ds.getConnection();
    }

    HikariDataSource getDataSource() {
        val config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + data);
        config.setUsername(user);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }
}
