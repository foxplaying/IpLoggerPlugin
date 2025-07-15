// IpDatabase.java
package com.foxplaying.iplogger;

import java.sql.*;

public class IpDatabase {
    private final IpLoggerPlugin plugin;
    private Connection conn;

    public IpDatabase(IpLoggerPlugin plugin) {
        this.plugin = plugin;
    }

    private String getLogMessage(String key) {
        return plugin.getLangMessage(key);
    }

    public void init() {
        try {
            String dbPath = plugin.getDataFolder() + "/ipdata.db";
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            plugin.getLogger().info(getLogMessage("db-connection-success"));

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS iplog (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "ipv4_ip TEXT, " +
                    "ipv6_ip TEXT, " +
                    "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );
            }
        } catch (Exception e) {
            plugin.getLogger().warning(getLogMessage("db-connection-failed") + e.getMessage());
            conn = null;
        }
    }

    public void savePlayerIp(String uuid, String name, String ipv4, String ipv6) {
        if (conn == null) {
            plugin.getLogger().warning(getLogMessage("db-not-connected"));
            return;
        }

        String unknownText = plugin.getLangMessage("unknown");

        if ((ipv4 == null || ipv4.equalsIgnoreCase(unknownText)) &&
            (ipv6 == null || ipv6.equalsIgnoreCase(unknownText))) {
            plugin.getLogger().warning(getLogMessage("public-ip-failed"));
            return;
        }

        try {
            String querySql = "SELECT ipv4_ip, ipv6_ip FROM iplog WHERE uuid = ?";
            try (PreparedStatement queryStmt = conn.prepareStatement(querySql)) {
                queryStmt.setString(1, uuid);
                ResultSet rs = queryStmt.executeQuery();

                if (rs.next()) {
                    String oldIpv4 = rs.getString("ipv4_ip");
                    String oldIpv6 = rs.getString("ipv6_ip");

                    boolean sameIpv4 = (oldIpv4 == null && ipv4 == null) || (oldIpv4 != null && oldIpv4.equals(ipv4));
                    boolean sameIpv6 = (oldIpv6 == null && ipv6 == null) || (oldIpv6 != null && oldIpv6.equals(ipv6));

                    if (sameIpv4 && sameIpv6) {
                        return;
                    }

                    try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE iplog SET name = ?, ipv4_ip = ?, ipv6_ip = ?, time = CURRENT_TIMESTAMP WHERE uuid = ?"
                    )) {
                        updateStmt.setString(1, name);
                        updateStmt.setString(2, ipv4);
                        updateStmt.setString(3, ipv6);
                        updateStmt.setString(4, uuid);
                        updateStmt.executeUpdate();
                    }

                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO iplog (uuid, name, ipv4_ip, ipv6_ip, time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)"
                    )) {
                        insertStmt.setString(1, uuid);
                        insertStmt.setString(2, name);
                        insertStmt.setString(3, ipv4);
                        insertStmt.setString(4, ipv6);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning(getLogMessage("ip-record-failed") + e.getMessage());
        }
    }

    public IpRecord getLatestIpInfo(String name) {
        if (conn == null) {
            plugin.getLogger().warning(getLogMessage("db-not-connected"));
            return null;
        }

        String sql = "SELECT uuid, ipv4_ip, ipv6_ip, time FROM iplog WHERE name = ? COLLATE NOCASE LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String uuid = rs.getString("uuid");
                String ipv4 = rs.getString("ipv4_ip");
                String ipv6 = rs.getString("ipv6_ip");
                String time = rs.getString("time");
                return new IpRecord(name, uuid, ipv4, ipv6, time);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning(getLogMessage("ip-query-failed") + e.getMessage());
        }
        return null;
    }

    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            plugin.getLogger().warning(getLogMessage("db-close-failed") + e.getMessage());
        }
    }

    public static class IpRecord {
        public final String name;
        public final String uuid;
        public final String ipv4;
        public final String ipv6;
        public final String time;

        public IpRecord(String name, String uuid, String ipv4, String ipv6, String time) {
            this.name = name;
            this.uuid = uuid;
            this.ipv4 = ipv4;
            this.ipv6 = ipv6;
            this.time = time;
        }
    }
}
