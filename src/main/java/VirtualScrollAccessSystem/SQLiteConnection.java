package VirtualScrollAccessSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public enum SQLiteConnection {
    INSTANCE("jdbc:sqlite:src/main/resources/db/rates.db");
    //    Source: https://www.javatpoint.com/java-sqlite
    private String db_path;

    SQLiteConnection(String db_path) {
        this.db_path = db_path;
    }

    public void change_path(String new_db_path) {
        this.db_path = new_db_path;
    }

    public void connect() {
        Connection conn1 = null;
        try {
            // Create a connection to the SQL database
            conn1 = DriverManager.getConnection(db_path);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn1 != null) {
                    conn1.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void initializeTables() {
        // SQL statement to create a user table to store user info
        String sqlTableUsers = "CREATE TABLE IF NOT EXISTS user (" +
                "phone_number VARCHAR(20), " +
                "email_address VARCHAR(255), " +
                "full_name VARCHAR(100), " +
                "ID VARCHAR(30) NOT NULL, " +
                "username VARCHAR(8) UNIQUE NOT NULL, " +
                "psw VARCHAR(255) NOT NULL, " +
                "uploads INTEGER NOT NULL, " +
                "downloads INTEGER NOT NULL, " +
                "PRIMARY KEY(ID)" +
                ");";

        // SQL statement to create a scroll table to store all uploaded scroll info
        String sqlTableScrolls = "CREATE TABLE IF NOT EXISTS scrolls (" +
                "title VARCHAR(255) NOT NULL, " +
                "creator_ID VARCHAR(30) NOT NULL, " +
                "scroll_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date DATE NOT NULL, " +
                "time INTEGER NOT NULL, " +
                "file_data BLOB NOT NULL, " +
                "scroll_of_day INTEGER NOT NULL, " +
                "password VARCHAR(255) " +
                ");";

        // SQL statement to create a scroll table to store all uploaded scroll info
        String sqlTableScrolls2 = "CREATE TABLE IF NOT EXISTS scrollsHistory (" +
                "title VARCHAR(255), " +
                "uploads INTEGER NOT NULL, " +
                "downloads INTEGER NOT NULL, " +
                "PRIMARY KEY(title)" +
                ");";

        try{
            Connection conn1 = DriverManager.getConnection(db_path);
            Statement stmt1 = conn1.createStatement();
            stmt1.execute(sqlTableUsers);
            stmt1.execute(sqlTableScrolls);
            stmt1.execute(sqlTableScrolls2);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
