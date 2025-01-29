package VirtualScrollAccessSystem;

import java.io.*;
import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public enum SQLiteOperations {
    INSTANCE("jdbc:sqlite:src/main/resources/db/rates.db");
    private String db_path;

    SQLiteOperations(String db_path) {
        this.db_path = db_path;
    }

    public void change_path(String new_db_path) {
        this.db_path = new_db_path;
    }

    private Connection connect() {
        // SQLite connection string
        String url = db_path;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    // Converts Java Date to SQL date to insert into DB
    public Date convertJavaDateToSQL(String date) {
        // Define date formatter to match string format - dd/MM/yyyy
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(date, formatter);

            // convert to java.sql.Date
            return Date.valueOf(localDate);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Set the timezone, get the local time and adjust the format
    public int getTimeInSeconds() {
        ZoneId zoneId = ZoneId.of("Australia/Sydney");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        LocalTime localTime = zonedDateTime.toLocalTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        return localTime.toSecondOfDay();
    }

    // Insert a user
    public int insert_user(String number, String email, String name, String ID, String username, String psw) throws SQLException {
        String sql = "INSERT INTO user VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String encoded_psw = encrypt_psw(psw);
            pstmt.setString(1, number);
            pstmt.setString(2, email);
            pstmt.setString(3, name);
            pstmt.setString(4, ID);
            pstmt.setString(5, username);
            pstmt.setString(6, encoded_psw);
            pstmt.setInt(7, 0);
            pstmt.setInt(8, 0);

            pstmt.executeUpdate();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());

            if(e.getMessage().contains("A PRIMARY KEY constraint failed (UNIQUE constraint failed: user.ID"))
                return -1;
            else if (e.getMessage().contains("A UNIQUE constraint failed (UNIQUE constraint failed: user.username"))
                return -2;
            else
                return 0;
        }
    }

    public int change_psw(String psw, String userID) throws SQLException {
        String sql = "UPDATE user SET psw = ? WHERE ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, encrypt_psw(psw));
            pstmt.setString(2, userID);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                return 0;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public int update_user_details(String userID, String phoneNumber, String email, String name, String username, String psw)
            throws SQLException {
        String sql = "SELECT psw FROM user WHERE ID = ?";
        String update_sql = "UPDATE user SET phone_number = ?, email_address = ?, full_name = ?, username = ? WHERE ID = ?";

        String resultPsw = null;

        // Update password
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userID);

            ResultSet result = pstmt.executeQuery();

            resultPsw = result.getString("psw");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 1;
        }

        // If a new password is given, update it
        if(resultPsw != null && !resultPsw.equals(psw))
            change_psw(psw, userID);

        // Update other details
        try (Connection conn = this.connect();
             PreparedStatement pstmt_update = conn.prepareStatement(update_sql)) {
            pstmt_update.setString(1, phoneNumber);
            pstmt_update.setString(2, email);
            pstmt_update.setString(3, name);
            pstmt_update.setString(4, username);
            pstmt_update.setString(5, userID);
            int result_update = pstmt_update.executeUpdate();


            if (result_update > 0) {
                return 0;
            } else {
                return -1;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -2;
        }
    }

    public String encrypt_psw(String psw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded_hash = digest.digest(psw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encoded_hash);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public int del_user(String username) {
        String sql = "DELETE FROM user WHERE ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                return 1;
            } else {
                return 2;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public List<String> all_users_usernames() {
        String sql = "SELECT username FROM user";
        List<String> usernames = new ArrayList<>(); // Create the list outside the try block

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet result = pstmt.executeQuery()) {

            while (result.next()) {
                usernames.add(result.getString("username"));
            }

            return usernames;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return null;
        }
    }

    // Get no. of uploads for a user
    public int get_user_uploads(String userID) {
        String sql = "SELECT uploads FROM user WHERE ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userID);
            ResultSet result = pstmt.executeQuery();

            return result.getInt("uploads");

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return -1;
        }
    }


    // Get no. of downloads for a user
    public int get_user_downloads(String userID) {
        String sql = "SELECT downloads FROM user WHERE ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userID);
            ResultSet result = pstmt.executeQuery();

            return result.getInt("downloads");

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return -1;
        }
    }

    // Get user ID for a user
    public String get_user_id(String username) {
        String sql = "SELECT ID FROM user WHERE username = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet result = pstmt.executeQuery();

            return result.getString("ID");

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return null;
        }
    }

    // Get details of a user for user profile
    public List<String> get_user_details(String userID) {
        String sql = "SELECT phone_number, email_address, full_name, username, psw FROM user WHERE ID = ?";
        List<String> userDetails = new ArrayList<>(); // Create the list outside the try block

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userID);

            ResultSet result = pstmt.executeQuery();

            userDetails.add(result.getString("phone_number"));
            userDetails.add(result.getString("email_address"));
            userDetails.add(result.getString("full_name"));
            userDetails.add(result.getString("username"));
            userDetails.add(result.getString("psw"));

            return userDetails;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return null;
        }
    }

    // Returns the ID of the current user to keep track of who is currently logged in
    public String user_login(String username, String psw) {
        String sql = "SELECT ID FROM user WHERE username = ? AND psw = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, encrypt_psw(psw));

            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                return result.getString("ID");
            } else {
                return null;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public int insert_scroll(String title, String creator_ID, String filePath, String date, String psw) {
        String sql = "INSERT INTO scrolls (title, creator_ID, date, time, file_data, scroll_of_day, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sql_increment = "SELECT uploads FROM user WHERE ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             InputStream fis = new FileInputStream(filePath)) {

            pstmt.setString(1, title);
            pstmt.setString(2, creator_ID);

            Date sql_date = convertJavaDateToSQL(date);
            pstmt.setDate(3, sql_date);
            int sqlTime = getTimeInSeconds();
            pstmt.setInt(4, sqlTime);

            pstmt.setBinaryStream(5, fis, fis.available());
            pstmt.setInt(6, 0);
            if(psw.isEmpty())
                pstmt.setString(7, null);
            else
                pstmt.setString(7, encrypt_psw(psw));

            pstmt.executeUpdate();

            try (PreparedStatement pstmt_increment = conn.prepareStatement(sql_increment)) {

                pstmt_increment.setString(1, creator_ID);
                ResultSet result = pstmt_increment.executeQuery();

                if (result.next()) {
                    int current_uploads = result.getInt("uploads");
                    int new_uploads = current_uploads + 1;

                    // Increment the value
                    sql = "UPDATE user SET uploads = ? WHERE ID = ?";

                    try (PreparedStatement pstmt2 = conn.prepareStatement(sql)) {
                        pstmt2.setInt(1, new_uploads);
                        pstmt2.setString(2, creator_ID);

                        int result2 = pstmt2.executeUpdate();
                    }
                } else {
                    System.out.println("User not found.");
                    return -2;
                }

            }

            String sql_history = "SELECT uploads FROM scrollsHistory WHERE title = ?";

            try (PreparedStatement pstmt_history = conn.prepareStatement(sql_history)) {

                pstmt_history.setString(1, title);
                ResultSet result = pstmt_history.executeQuery();

                if (result.next()) {
                    int current_uploads = result.getInt("uploads");
                    int new_uploads = current_uploads + 1;

                    // Increment the value
                    sql = "UPDATE scrollsHistory SET uploads = ? WHERE title = ?";

                    try (PreparedStatement pstmt2 = conn.prepareStatement(sql)) {
                        pstmt2.setInt(1, new_uploads);
                        pstmt2.setString(2, title);

                        int result2 = pstmt2.executeUpdate();
                    }
                } else {
                    // Increment the value
                    sql = "INSERT INTO scrollsHistory (title, uploads, downloads) VALUES (?, ?, ?)";

                    try (PreparedStatement pstmt2 = conn.prepareStatement(sql)) {
                        pstmt2.setString(1, title);
                        pstmt2.setInt(2, 1);
                        pstmt2.setInt(3, 0);

                        int result2 = pstmt2.executeUpdate();
                    }
                }

            }

            return 1;

        } catch (SQLException | IOException e) {
            System.out.println(e.getMessage());
            return -2;
        }
    }

    public int del_scroll(String title) {
        String sql = "DELETE FROM scrolls WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);

            int result = pstmt.executeUpdate();

            // Returns the number of scrolls deleted
            return result;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public List<String> get_scroll_titles() {
        String sql = "SELECT title FROM scrolls";
        List<String> titles = new ArrayList<>(); // Create the list outside the try block

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet result = pstmt.executeQuery()) {

            while (result.next()) {
                titles.add(result.getString("title"));
            }

            return titles;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return null;
        }
    }

    public List<String> get_scroll_history_titles() {
        String sql = "SELECT title FROM scrollsHistory";
        List<String> titles = new ArrayList<>(); // Create the list outside the try block

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet result = pstmt.executeQuery()) {

            while (result.next()) {
                titles.add(result.getString("title"));
            }

            return titles;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return null;
        }
    }

    public List<String> get_scroll_titles_filter(String key, String value) {
        String sql;

        // Case insensitivity for strings
        if(key.equals("title") || key.equals("creator_id"))
            sql = "SELECT title FROM scrolls WHERE lower(\"" + key + "\") = lower(?)";
        else
            sql = "SELECT title FROM scrolls WHERE \"" + key + "\" = ?";

        List<String> titles = new ArrayList<>(); // Create the list outside the try block

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if(key.equals("date")) {
                Date sql_date = convertJavaDateToSQL(value);
                pstmt.setDate(1, sql_date);
            } else
                pstmt.setString(1, value);


            ResultSet result = pstmt.executeQuery();

            while (result.next()) {
                titles.add(result.getString("title"));
            }

            return titles;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return null;
        }
    }

    // Get the scroll content as base64 string
    public String get_scroll_content(String title) {
        String sql = "SELECT file_data FROM scrolls WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                byte[] binaryData = result.getBytes("file_data");
                return Base64.getEncoder().encodeToString(binaryData);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return null;
    }

    // Retrieving raw binary data for download
    public byte[] get_scroll_content_as_bytes(String title) {
        String sql = "SELECT file_data FROM scrolls WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                // Get the binary data as a byte array
                return result.getBytes("file_data");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        return null; // Return null if there's an error or no data found
    }

    public String get_creatorid(String title) {
        String sql = "SELECT creator_ID FROM scrolls WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setString(1, title);

            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                return result.getString("creator_ID");
            } else {
                return null;
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return null;
        }
    }

    // Set a new random scroll of the day (for when admin deletes the scroll of the day)
    public void set_new_scroll_day() {
        String sql = "SELECT * FROM scrolls WHERE scroll_of_day = ?";
        String update_sql = "UPDATE scrolls SET scroll_of_day = ? WHERE scroll_of_day = ?";
        String sql_random = "SELECT scroll_ID, creator_ID FROM scrolls WHERE password IS NULL ORDER BY RANDOM() LIMIT 1";
        String sql_new_scroll = "UPDATE scrolls SET scroll_of_day = ? WHERE scroll_ID = ? AND creator_ID = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setInt(1, 1);

            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                try (PreparedStatement pstmt_update = conn.prepareStatement(update_sql)) {
                    pstmt_update.setInt(1, 0);
                    pstmt_update.setInt(2, 1);

                    pstmt_update.executeUpdate();

                }
            }

            try (PreparedStatement pstmt_random = conn.prepareStatement(sql_random)) {
                ResultSet result_random = pstmt_random.executeQuery();

                if (result_random.next()) {
                    String scroll_id = result_random.getString("scroll_ID");
                    String creator_id = result_random.getString("creator_ID");

                    try (PreparedStatement pstmt_new = conn.prepareStatement(sql_new_scroll)) {
                        pstmt_new.setInt(1, 1);
                        pstmt_new.setString(2, scroll_id);
                        pstmt_new.setString(3, creator_id);

                        int result_new = pstmt_new.executeUpdate();

                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Set a known scroll of the day (for Set Scroll of the Day Button)
    public void set_scroll_day(String title) {
        String sql = "SELECT * FROM scrolls WHERE scroll_of_day = ?";
        String update_sql = "UPDATE scrolls SET scroll_of_day = ? WHERE scroll_of_day = ?";
        String sql_new_scroll = "UPDATE scrolls SET scroll_of_day = ? WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setInt(1, 1);

            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                try (PreparedStatement pstmt_update = conn.prepareStatement(update_sql)) {
                    pstmt_update.setInt(1, 0);
                    pstmt_update.setInt(2, 1);
                    pstmt_update.executeUpdate();

                }
            }

            try (PreparedStatement pstmt_new = conn.prepareStatement(sql_new_scroll)) {
                pstmt_new.setInt(1, 1);
                pstmt_new.setString(2, title);

                int result_new = pstmt_new.executeUpdate();

            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String get_scroll_of_day() {
        String sql = "SELECT title FROM scrolls WHERE scroll_of_day = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);) {

            pstmt.setInt(1, 1);

            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                return result.getString("title");
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return null;
        }
    }

    // Update scroll content using base64 string
    public int update_scroll_content(String title, String content) {
        String sql = "UPDATE scrolls SET file_data = ? WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Decode base64 string into binary data
            byte[] binaryData = Base64.getDecoder().decode(content);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(binaryData);
            pstmt.setBinaryStream(1, inputStream, binaryData.length);
            pstmt.setString(2, title);

            int result = pstmt.executeUpdate();

            // Returns the number of scrolls updated (0 if none)
            return result;


        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return -1;
        }
    }

    public int increment_downloads(String title, String userID) {
        // First get the current number of downloads
        String sql = "SELECT downloads FROM scrollsHistory WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                int currentDownloads = result.getInt("downloads");
                int newDownloads = currentDownloads + 1;

                // Increment the value
                sql = "UPDATE scrollsHistory SET downloads = ? WHERE title = ?";

                try (PreparedStatement pstmt2 = conn.prepareStatement(sql)) {
                    pstmt2.setInt(1, newDownloads);
                    pstmt2.setString(2, title);

                    int result2 = pstmt2.executeUpdate();

                    // Now to increment the user's download value
                    sql = "SELECT downloads FROM user WHERE ID = ?";

                    try (PreparedStatement pstmt_increment = conn.prepareStatement(sql)) {

                        pstmt_increment.setString(1, userID);
                        ResultSet result3 = pstmt_increment.executeQuery();

                        if (result3.next()) {
                            int current_downloads = result3.getInt("downloads");
                            int new_downloads = current_downloads + 1;

                            // Increment the value
                            sql = "UPDATE user SET downloads = ? WHERE ID = ?";

                            try (PreparedStatement pstmt3 = conn.prepareStatement(sql)) {
                                pstmt3.setInt(1, new_downloads);
                                pstmt3.setString(2, userID);

                                int result4 = pstmt3.executeUpdate();
                            }
                        } else {
                            System.out.println("User not found.");
                            return -4;
                        }
                    }
                    // Return the number of rows affected (0 if none)
                    return result2;
                }
            } else {
                System.out.println("Scroll not found.");
                return -1;
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return -2;
        }
    }

    // Returns 1 if the password is correct
    public int access_scroll_psw(String title, String psw) {
        String sql = "SELECT password FROM scrolls WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                String password = result.getString("password");
                if (password == null || password.equals(encrypt_psw(psw))) {
                    return 1;
                }
                else {
                    return 0;
                }
            } else {
                System.out.println("Scroll not found.");
                return -1;
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return -2;
        }
    }

    // Get the password for a scroll
    public String get_scroll_psw(String title) {
        String sql = "SELECT password FROM scrolls WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet result = pstmt.executeQuery();

            String password = result.getString("password");

            return password;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return null;
        }
    }

    // Get no. of uploads for a scroll
    public int get_scroll_uploads(String title) {
        String sql = "SELECT uploads FROM scrollsHistory WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet result = pstmt.executeQuery();

            int uploads = result.getInt("uploads");

            return uploads;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return -1;
        }
    }

    // Get no. of downloads for a scroll
    public int get_scroll_downloads(String title) {
        String sql = "SELECT downloads FROM scrollsHistory WHERE title = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            ResultSet result = pstmt.executeQuery();

            int downloads = result.getInt("downloads");

            return downloads;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return -1;
        }
    }
}
