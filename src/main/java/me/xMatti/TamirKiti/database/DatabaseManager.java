package me.xMatti.TamirKiti.database;

import me.xMatti.TamirKiti.TamirKitiMain;
import org.bukkit.ChatColor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final TamirKitiMain plugin;
    private Connection connection;

    private final String HOST;
    private final int PORT;
    private final String DATABASE;
    private final String USER;
    private final String PASSWORD;
    private final String DB_TYPE;

    public DatabaseManager(TamirKitiMain plugin) {
        this.plugin = plugin;

        this.DB_TYPE = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();

        if (this.DB_TYPE.equals("MYSQL")) {
            this.HOST = plugin.getConfig().getString("database.mysql.host", "localhost");
            this.PORT = plugin.getConfig().getInt("database.mysql.port", 3306);
            this.DATABASE = plugin.getConfig().getString("database.mysql.database", "tamir_kiti_db");
            this.USER = plugin.getConfig().getString("database.mysql.user", "root");
            this.PASSWORD = plugin.getConfig().getString("database.mysql.password", "");
        } else {
            this.HOST = null;
            this.PORT = 0;
            this.DATABASE = null;
            this.USER = null;
            this.PASSWORD = null;
        }
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        if (DB_TYPE.equals("MYSQL")) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false&autoReconnect=true";
                connection = DriverManager.getConnection(url, USER, PASSWORD);
                plugin.getLogger().info(ChatColor.GOLD + "MySQL veritabanına bağlantı başarılı.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC sürücüsü bulunamadı! Lütfen 'mysql-connector-java.jar' dosyasını sunucu /lib klasörüne ekleyin.", e);
            }
        } else {
            try {
                Class.forName("org.sqlite.JDBC");
                String sqlitePath = "jdbc:sqlite:" + new java.io.File(plugin.getDataFolder(), "cooldowns.db").getAbsolutePath();
                connection = DriverManager.getConnection(sqlitePath);
                plugin.getLogger().info(ChatColor.GOLD + "SQLite veritabanına bağlantı başarılı.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC sürücüsü bulunamadı! Lütfen 'sqlite-jdbc.jar' dosyasını sunucu /lib klasörüne ekleyin.", e);
            }
        }
    }

    public void createTable() {
        String sql;
        if (DB_TYPE.equals("MYSQL")) {
            sql = "CREATE TABLE IF NOT EXISTS player_cooldowns (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "timestamp BIGINT NOT NULL" +
                    ");";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS player_cooldowns (" +
                    "uuid TEXT PRIMARY KEY," +
                    "timestamp INTEGER NOT NULL" +
                    ");";
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            plugin.getLogger().info(ChatColor.GOLD + "Veritabanı tablosu başarıyla oluşturuldu/doğrulandı.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "Veritabanı tablosu oluşturulurken hata oluştu! " + DB_TYPE + ":", e);
        }
    }

    public Map<UUID, Long> loadAllCooldowns() {
        Map<UUID, Long> cooldowns = new HashMap<>();
        String sql = "SELECT uuid, timestamp FROM player_cooldowns;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    long timestamp = rs.getLong("timestamp");
                    cooldowns.put(uuid, timestamp);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning(ChatColor.YELLOW + "Veritabanında geçersiz UUID formatı bulundu: " + rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "Cooldown verileri veritabanından yüklenirken hata oluştu! " + DB_TYPE + ":", e);
        }
        return cooldowns;
    }

    public void setPlayerCooldown(UUID uuid, long timestamp) {
        String sql;
        if (DB_TYPE.equals("MYSQL")) {
            sql = "INSERT INTO player_cooldowns (uuid, timestamp) VALUES (?, ?) ON DUPLICATE KEY UPDATE timestamp = VALUES(timestamp);";
        } else {
            sql = "INSERT OR REPLACE INTO player_cooldowns (uuid, timestamp) VALUES (?, ?);";
        }
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setLong(2, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "Oyuncu (" + uuid + ") bekleme süresi veritabanına kaydedilemedi! " + DB_TYPE + ":", e);
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    plugin.getLogger().info(ChatColor.GOLD + DB_TYPE + " veritabanı bağlantısı kapatıldı.");
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, ChatColor.RED + DB_TYPE + " veritabanı bağlantısı kapatılırken hata oluştu!", e);
            }
        }
    }
}