package me.ellbristow.mychunk.utils.db;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SQLBridge {

    private static Plugin plugin;
    private static Connection conn;
    private static File sqlFile;
    private static Statement statement;
    private static String host;
    private static String port;
    private static String db;
    private static String user;
    private static String pass;
    private static boolean isMySQL = false;
    private static String connectionString = "";
    
    public static void initSQLite () {
        
        plugin = Bukkit.getPluginManager().getPlugin("MyChunk");
        sqlFile = new File("plugins/" + plugin.getDataFolder().getName() + File.separator + plugin.getName() + ".db");
        connectionString = "jdbc:sqlite:" + sqlFile.getAbsolutePath();
        
        if (!sqlFile.exists()) {
            try {
                sqlFile.createNewFile();
            } catch (IOException ex) {
                plugin.getLogger().severe("Error creating SQLite Database File! Disabling Plugin...");
                ex.printStackTrace();
                plugin.getPluginLoader().disablePlugin(plugin);
            }
        }
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            
            plugin.getLogger().severe("Error initialising SQLite database driver! Disabling plugin...");
            ex.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
            
        }
        
    }
    
    public static void initMySQL (String hostname, String portnumber, String database, String username, String password) {
        
        isMySQL = true;
        host = hostname;
        port = portnumber;
        db = database;
        user = username;
        pass = password;
        
        plugin = Bukkit.getPluginManager().getPlugin("MyChunk");
        
        connectionString = "jdbc:mysql://" + host + ":" + port + "/" + db;
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            
            plugin.getLogger().severe("Error initialising MySQL database driver! Disabling plugin...");
            ex.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
            
        }
        
    }
    
    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                return open();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
    
    public static Connection open() {
        try {
            if (conn == null || conn.isClosed()) {
                if (isMySQL) {
                    conn = DriverManager.getConnection(connectionString, user, pass);
                } else {
                    conn = DriverManager.getConnection(connectionString);
                }
            }
            return conn;
        } catch (Exception e) {
            plugin.getLogger().severe("Error opening database connection!");
            e.printStackTrace();
        }
        return null;
    }
    
    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error closing database connection!");
            e.printStackTrace();
        }
    }
    
    public static boolean checkTable(String tableName) {
        DatabaseMetaData dbm;
        try {
            dbm = open().getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            if (tables.next()) {
                close();
                return true;
            } else {
                close();
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error while checking table '"+tableName+"' existence!");
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean createTable(String tableName, String[] columns, String[] dims) {
        String query = "";
        try {
            if (conn == null || conn.isClosed())
                open();
            statement = conn.createStatement();
            query = "CREATE TABLE `" + tableName + "` (";
            for (int i = 0; i < columns.length; i++) {
                if (i!=0) {
                    query += ", ";
                }
                query += columns[i] + " " + dims[i];
            }
            query += ")";
            statement.execute(query);
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating table '"+tableName+"'!");
            plugin.getLogger().severe(query);
            e.printStackTrace();
        }
        close();
        return true;
    }
    
    public static void query(String query) {
        try {
            if (conn == null || conn.isClosed())
                open();
            statement = conn.createStatement();
            statement.executeUpdate(query);
            close();
        } catch (Exception e) {
            close();
            queryRetry(query);
        }
    }
    
    private static void queryRetry(String query) {
        try {
            if (conn == null || conn.isClosed())
                open();
            statement = conn.createStatement();
            statement.executeUpdate(query);
            close();
        } catch (Exception e) {
            close();
            plugin.getLogger().severe("Database query error!");
            plugin.getLogger().severe(query);
            e.printStackTrace();
        }
    }
    
    public static HashMap<Integer, HashMap<String, String>> select(String fields, String tableName, String where, String group, String order) {
        HashMap<Integer, HashMap<String, String>> rows = new HashMap<Integer, HashMap<String, String>>();
        if ("".equals(fields) || fields == null) {
            fields = "*";
        }
        String query = "SELECT " + fields + " FROM " + tableName;
        try {
            if (conn == null || conn.isClosed())
                open();
            statement = conn.createStatement();
            if (!"".equals(where) && where != null) {
                query += " WHERE " + where;
            }
            if (!"".equals(group) && group != null) {
                query += " GROUP BY " + group;
            }
            if (!"".equals(order) && order != null) {
                query += " ORDER BY " + order;
            }
            ResultSet results = statement.executeQuery(query);
            if (results != null) {
                int columns = results.getMetaData().getColumnCount();
                String columnNames = "";
                for (int i = 1; i <= columns; i++) {
                    if (!"".equals(columnNames)) {
                        columnNames += ",";
                    }
                    columnNames += results.getMetaData().getColumnName(i);
                }
                String[] columnArray = columnNames.split(",");
                int numRows = 0;
                while (results.next()) {
                    HashMap<String, String> thisColumn = new HashMap<String, String>();
                    for (String columnName : columnArray) {
                        thisColumn.put(columnName, results.getString(columnName));
                    }
                    rows.put(numRows, thisColumn);
                    numRows++;
                }
                results.close();
                close();
                return rows;
            } else {
                results.close();
                close();
                return null;
            }
        } catch (Exception e) {
            close();
            return selectRetry(fields, tableName, where, group, order);
        }
    }
    
    public static HashMap<Integer, HashMap<String, String>> selectRetry(String fields, String tableName, String where, String group, String order) {
        HashMap<Integer, HashMap<String, String>> rows = new HashMap<Integer, HashMap<String, String>>();
        if ("".equals(fields) || fields == null) {
            fields = "*";
        }
        String query = "SELECT " + fields + " FROM " + tableName;
        try {
            if (conn == null || conn.isClosed())
                open();
            statement = conn.createStatement();
            if (!"".equals(where) && where != null) {
                query += " WHERE " + where;
            }
            if (!"".equals(group) && group != null) {
                query += " GROUP BY " + group;
            }
            if (!"".equals(order) && order != null) {
                query += " ORDER BY " + order;
            }
            ResultSet results = statement.executeQuery(query);
            if (results != null) {
                int columns = results.getMetaData().getColumnCount();
                String columnNames = "";
                for (int i = 1; i <= columns; i++) {
                    if (!"".equals(columnNames)) {
                        columnNames += ",";
                    }
                    columnNames += results.getMetaData().getColumnName(i);
                }
                String[] columnArray = columnNames.split(",");
                int numRows = 0;
                while (results.next()) {
                    HashMap<String, String> thisColumn = new HashMap<String, String>();
                    for (String columnName : columnArray) {
                        thisColumn.put(columnName, results.getString(columnName));
                    }
                    rows.put(numRows, thisColumn);
                    numRows++;
                }
                results.close();
                close();
                return rows;
            } else {
                results.close();
                close();
                return null;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Database select error!");
            plugin.getLogger().severe(query);
            e.printStackTrace();
        }
        close();
        return null;
    }
    
    public static boolean tableContainsColumn(String tableName, String columnName) {
        try {
            if (conn == null || conn.isClosed())
                open();
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT `" + columnName + "` FROM `" + tableName + "` LIMIT 1");
            if (rs == null) {
                close();
                return false;
            }
            close();
        } catch (SQLException e) {
            if (e.getMessage().contains("no such column: " + columnName)) {
                return false;
            }
            plugin.getLogger().severe("Error check table columns in '"+tableName+"'!");
            e.printStackTrace();
        }
        return true;
    }
    
    public static void addColumn(String tableName, String columnDef) {
        try {
            if (conn == null || conn.isClosed())
                open();
            statement = conn.createStatement();
            statement.executeUpdate("ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnDef + "`");
            statement.close();
            close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding column to '"+tableName+"'!");
            e.printStackTrace();
        }
    }
    
}
