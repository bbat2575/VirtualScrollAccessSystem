package VirtualScrollAccessSystem;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewScrollsPage {
    private Stage stage;
    private String userID;
    private String currentScroll;

    public ViewScrollsPage(Stage stage, String userID) {
        this.stage = stage;
        this.userID = userID;
        currentScroll = null;
    }

    public void load(List<String> titles) {
        // Create titles
        Label title = new Label("View Scrolls");
        title.setId("title");
        Label usersTitle = new Label("Users");
        usersTitle.setId("usersTitle");

        if(!userID.equals("admin"))
            usersTitle.setVisible(false);

        Label userIDLabel = new Label("User ID: " + userID);
        Label userTypeLabel = new Label("User Type: ");
        Button viewProfileButton = new Button();
        Button logoutButton = new Button("Logout");

        // Create user details and logout/go back buttons
        if(!userID.equals("guest"))
            viewProfileButton.setText("View Profile");

        // Labels for the no. of uploads and downloads for a scroll
        Label uploadsLabel = new Label();
        Label downloadsLabel = new Label();

        // Add dropdown box for search options
        Label searchLabel = new Label("Search Options:");
        ComboBox<String> searchComboBox = new ComboBox<>();
        searchComboBox.getItems().addAll("Title", "Creator ID", "Scroll ID", "Upload Date");
        TextField searchTextField = new TextField();
        searchTextField.setPrefWidth(113);
        searchTextField.setMaxWidth(113);
        searchTextField.setMinWidth(113);
        Button searchButton = new Button("Search");

        // Create other buttons
        Button finalizeButton = new Button("Finalize");
        finalizeButton.setVisible(false);
        Button addScrollButton = new Button("Add Scroll");
        addScrollButton.setVisible(false);
        Button setSodButton = new Button("Set ");
        setSodButton.setVisible(false);

        ImageView setSodImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/star.png")));
        setSodImageView.setFitWidth(20);
        setSodImageView.setFitHeight(20);
        setSodImageView.setPreserveRatio(true);
        setSodButton.setGraphic(setSodImageView);
        setSodButton.setContentDisplay(ContentDisplay.RIGHT);

        // Password field for locked scrolls
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setVisible(false);
        Label resultOutput = new Label();
        resultOutput.setPrefWidth(133);
        resultOutput.setId("resultOutput2");

        // Scroll of the day legend
        Label scrollOfDayLabel = new Label("Scroll Of Day = ");

        // Create ImageView for scroll of the day legend
        ImageView sodLegendImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/star.png")));
        sodLegendImageView.setFitWidth(20);
        sodLegendImageView.setFitHeight(20);
        sodLegendImageView.setPreserveRatio(true);

        // Set label for admin, user and guest
        if(userID.equals("admin"))
            userTypeLabel.setText(userTypeLabel.getText() + "Admin");
        else if(userID.equals("guest"))
            userTypeLabel.setText(userTypeLabel.getText() + "Guest");
        else
            userTypeLabel.setText(userTypeLabel.getText() + "User");

        // Create a ScrollPane to hold the scrolls
        ScrollPane scrollBox = new ScrollPane();
        scrollBox.setPrefSize(365, 210); // width and height of scrollBox
        scrollBox.setMaxHeight(210);
        scrollBox.setMinHeight(210);
        scrollBox.setPannable(true);

        // Create a VBox with transparent background to hold scrolls
        VBox allScrolls = new VBox(3);
        allScrolls.setPadding(new Insets(3));

        // Create a list to store historical titles
        List<String> historyTitles = null;

        // Retrieve scroll titles from the database
        if(titles == null) {
            titles = SQLiteOperations.INSTANCE.get_scroll_titles();
            historyTitles = SQLiteOperations.INSTANCE.get_scroll_history_titles();

            // Remove any current scroll titles from the history titles list
            historyTitles.removeAll(titles);
        }

        // Create an area to view/edit scroll content
        TextArea scrollViewArea = new TextArea();
        scrollViewArea.setPrefSize(365, 100);
        scrollViewArea.setMaxHeight(100);
        scrollViewArea.setMinHeight(100);
        scrollViewArea.setWrapText(true);
        scrollViewArea.setEditable(false);
        scrollViewArea.setId("scrollViewArea");

        // Add each scroll title as a clickable, hoverable rectangle
        for (String scrollTitle : titles) {
            Button downloadButton = new Button();
            downloadButton.getStyleClass().add("scroll-buttons");
            downloadButton.setMinSize(25, 25);
            downloadButton.setMaxSize(25, 25);
            downloadButton.setPrefSize(25, 25);
            downloadButton.setVisible(false);

            Button deleteButton = new Button();
            deleteButton.getStyleClass().add("scroll-buttons");
            deleteButton.setMinSize(25, 25);
            deleteButton.setMaxSize(25, 25);
            deleteButton.setPrefSize(25, 25);
            deleteButton.setVisible(false);

            // Set privileges/buttons (GUESTS: can only view scrolls (not download or upload etc.))
            if(!userID.equals("guest")) {
                // Users and Admin: can upload/download any scrolls (without leaving the view scrolls page)

                // Make the addScrollButton available
                addScrollButton.setVisible(true);

                // Create download button
                ImageView downloadImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/download.png")));
                downloadImageView.setFitWidth(25);
                downloadImageView.setFitHeight(25);
                downloadImageView.setPreserveRatio(true);
                downloadButton.setGraphic(downloadImageView);
                downloadButton.setVisible(true);
                downloadButton.setOnAction(event -> downloadScroll(scrollTitle, stage));

                if(SQLiteOperations.INSTANCE.get_creatorid(scrollTitle).equals(userID) || userID.equals("admin")) {
                    // Create delete button
                    ImageView deleteImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
                    deleteImageView.setFitWidth(15);
                    deleteImageView.setFitHeight(15);
                    deleteImageView.setPreserveRatio(true);
                    deleteButton.setGraphic(deleteImageView);
                    deleteButton.setVisible(true);
                    deleteButton.setOnAction(e -> {
                        String scroll_of_day = SQLiteOperations.INSTANCE.get_scroll_of_day();
                        SQLiteOperations.INSTANCE.del_scroll(scrollTitle);

                        if(scrollTitle.equals(scroll_of_day)) {
                            SQLiteOperations.INSTANCE.set_new_scroll_day();
                        }
                        this.load(null);
                    });
                }
            }

            // Rectangle with partial transparency
            Rectangle scroll = new Rectangle(340, 30, Color.web("#d4c8ba"));
            scroll.setArcWidth(10);
            scroll.setArcHeight(10);
            scroll.setStroke(Color.BLACK);
            scroll.setStrokeWidth(0.5);

            // Create ImageView for scroll of the day star
            ImageView sodImageView = new ImageView();
            sodImageView.setFitWidth(20);
            sodImageView.setFitHeight(20);
            sodImageView.setPreserveRatio(true);

            // Create ImageView for lock of locked scroll
            ImageView lockImageView = new ImageView();
            lockImageView.setFitWidth(20);
            lockImageView.setFitHeight(20);
            lockImageView.setPreserveRatio(true);

            try {
                if (SQLiteOperations.INSTANCE.get_scroll_of_day().equals(scrollTitle))
                    sodImageView.setImage(new Image(getClass().getResourceAsStream("/images/star.png")));
            } catch (Exception ignored) {} // for the rare case where there is no scroll of the day

            try {
                if (SQLiteOperations.INSTANCE.get_scroll_psw(scrollTitle) != null)
                    lockImageView.setImage(new Image(getClass().getResourceAsStream("/images/lock.png")));
            } catch (Exception ignored) {} // for the rare case where there is no scroll of the day

            // Text label for the title
            Text scrollText = new Text(scrollTitle);
            scrollText.setId("scrollText");

            // Use a HBox the display of a single scroll
            HBox scrollContent = new HBox(10, sodImageView, lockImageView, scrollText, downloadButton, deleteButton);
            scrollContent.setAlignment(Pos.CENTER);
            scrollContent.setPadding(new Insets(0, 20, 0, 35));

            // Stack text on the rectangle
            scroll.widthProperty().addListener((obs, oldVal, newVal) -> scrollText.setWrappingWidth(newVal.doubleValue() - 10));

            // Put both in a StackPane to center the scrollText
            StackPane scrollStackPane = new StackPane(scroll, scrollContent);
            scrollStackPane.setAlignment(Pos.CENTER);
            allScrolls.getChildren().add(scrollStackPane);

            // Make the scroll clickable
            scrollStackPane.setOnMouseClicked(event -> {
                // Users can edit or delete scrolls they've added. Admin can edit or delete any scrolls
                String scrollPsw = SQLiteOperations.INSTANCE.get_scroll_psw(scrollTitle);
                resultOutput.setText("");

                if(userID.equals(SQLiteOperations.INSTANCE.get_creatorid(scrollTitle)) || userID.equals("admin")) {
                    scrollViewArea.setEditable(true);
                    finalizeButton.setVisible(true);
                    scrollViewArea.setText(SQLiteOperations.INSTANCE.get_scroll_content(scrollTitle));

                    if(userID.equals("admin")) {
                        if(scrollPsw == null)
                            setSodButton.setVisible(true);
                        else
                            setSodButton.setVisible(false);

                        uploadsLabel.setText("Uploads: " + SQLiteOperations.INSTANCE.get_scroll_uploads(scrollTitle));
                        downloadsLabel.setText("Downloads: " + SQLiteOperations.INSTANCE.get_scroll_downloads(scrollTitle));
                    }

                } else {
                    scrollViewArea.setEditable(false);
                    finalizeButton.setVisible(false);

                    if(scrollPsw != null)
                        passwordField.setVisible(true);
                    else {
                        passwordField.setVisible(false);
                        scrollViewArea.setText(SQLiteOperations.INSTANCE.get_scroll_content(scrollTitle));
                    }
                }

                currentScroll = scrollTitle;
            });

            // Change cursor to hand on hover
            scrollStackPane.setOnMouseEntered(event -> scrollStackPane.setStyle("-fx-cursor: hand;"));
            scrollStackPane.setOnMouseExited(event -> scrollStackPane.setStyle("-fx-cursor: default;"));
        }

        // Admin can also see scrolls from history along with their upload and download stats
        if(historyTitles != null && userID.equals("admin")) {
            // Now add any historical scrolls
            for (String scrollTitle : historyTitles) {
                // Set button paddings to match current scrolls
                Button downloadButton = new Button();
                downloadButton.getStyleClass().add("scroll-buttons");
                downloadButton.setMinSize(25, 25);
                downloadButton.setMaxSize(25, 25);
                downloadButton.setPrefSize(25, 25);
                downloadButton.setVisible(false);

                Button deleteButton = new Button();
                deleteButton.getStyleClass().add("scroll-buttons");
                deleteButton.setMinSize(25, 25);
                deleteButton.setMaxSize(25, 25);
                deleteButton.setPrefSize(25, 25);
                deleteButton.setVisible(false);

                // Rectangle with partial transparency
                Rectangle scroll = new Rectangle(340, 30, Color.web("#ababab"));
                scroll.setArcWidth(10);
                scroll.setArcHeight(10);
                scroll.setStroke(Color.BLACK);
                scroll.setStrokeWidth(0.5);

                // Create ImageView padding for scroll of the day star
                ImageView sodImageView = new ImageView();
                sodImageView.setFitWidth(20);
                sodImageView.setFitHeight(20);
                sodImageView.setPreserveRatio(true);

                // Create ImageView padding for lock of locked scroll
                ImageView lockImageView = new ImageView();
                lockImageView.setFitWidth(20);
                lockImageView.setFitHeight(20);
                lockImageView.setPreserveRatio(true);

                // Text label for the title
                Text scrollText = new Text(scrollTitle);
                scrollText.setId("scrollText");

                // Use a HBox the display of a single scroll
                HBox scrollContent = new HBox(10, sodImageView, lockImageView, scrollText, downloadButton, deleteButton);
                scrollContent.setAlignment(Pos.CENTER);
                scrollContent.setPadding(new Insets(0, 20, 0, 35));

                // Stack text on the rectangle
                scroll.widthProperty().addListener((obs, oldVal, newVal) -> scrollText.setWrappingWidth(newVal.doubleValue() - 10));

                // Put both in a StackPane to center the scrollText
                StackPane scrollStackPane = new StackPane(scroll, scrollContent);
                scrollStackPane.setAlignment(Pos.CENTER);
                allScrolls.getChildren().add(scrollStackPane);

                // Make the scroll clickable
                scrollStackPane.setOnMouseClicked(event -> {
                    uploadsLabel.setText("Uploads: " + SQLiteOperations.INSTANCE.get_scroll_uploads(scrollTitle));
                    downloadsLabel.setText("Downloads: " + SQLiteOperations.INSTANCE.get_scroll_downloads(scrollTitle));

                    if(userID.equals("admin"))
                        setSodButton.setVisible(false);

                    currentScroll = scrollTitle;
                });

                // Change cursor to hand on hover
                scrollStackPane.setOnMouseEntered(event -> scrollStackPane.setStyle("-fx-cursor: hand;"));
                scrollStackPane.setOnMouseExited(event -> scrollStackPane.setStyle("-fx-cursor: default;"));
            }
        }

        // Set allScrolls VBox as the content of the scrollBox
        scrollBox.setContent(allScrolls);
        scrollBox.setFitToWidth(true);

        // Create a ScrollPane to hold the scrolls and add user button
        ScrollPane userBox = new ScrollPane();
        userBox.setPrefSize(160, 120); // width and height of userBox
        userBox.setMaxHeight(120);
        userBox.setMinHeight(120);
        userBox.setPannable(true);
        Button addUserButton = new Button("Add");

        if(!userID.equals("admin")) {
            userBox.setVisible(false);
            addUserButton.setVisible(false);
        }

        // Create a VBox with transparent background to hold scrolls
        VBox allUsers = new VBox(2);
        allUsers.setPadding(new Insets(2));
        allUsers.setAlignment(Pos.CENTER);

        List<String> usernames = SQLiteOperations.INSTANCE.all_users_usernames();

        // Admin also has a list of all users
        if(userID.equals("admin")) {
            // Add each user as a clickable, hoverable rectangle
            for (String username : usernames) {
                if(username.equals("admin"))
                    continue;

                // Create delete button
                Button deleteButton = new Button();
                deleteButton.getStyleClass().add("scroll-buttons");
                deleteButton.setMinSize(20, 20);
                deleteButton.setMaxSize(20, 20);
                deleteButton.setPrefSize(20, 20);
                deleteButton.setVisible(false);
                ImageView deleteImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));
                deleteImageView.setFitWidth(15);
                deleteImageView.setFitHeight(15);
                deleteImageView.setPreserveRatio(true);
                deleteButton.setGraphic(deleteImageView);
                deleteButton.setVisible(true);
                deleteButton.setOnAction(e -> {
                    SQLiteOperations.INSTANCE.del_user(SQLiteOperations.INSTANCE.get_user_id(username));
                    this.load(null);
                });

                // Rectangle with partial transparency
                Rectangle user = new Rectangle(170, 25, Color.web("#bde5f2"));
                user.setArcWidth(10);
                user.setArcHeight(10);
                user.setStroke(Color.BLACK);
                user.setStrokeWidth(0.5);

                // Text label for the username
                Text usernameText = new Text(username);
                usernameText.setId("scrollText");

                // Use a HBox the display of a single username
                HBox userContent = new HBox(10, usernameText, deleteButton);
                userContent.setAlignment(Pos.CENTER);
                userContent.setPadding(new Insets(0, 0, 0, 30));

                // Stack text on the rectangle
                user.widthProperty().addListener((obs, oldVal, newVal) -> usernameText.setWrappingWidth(newVal.doubleValue() - 10));

                // Put both in a StackPane to center the userText
                StackPane userStackPane = new StackPane(user, userContent);
                userStackPane.setAlignment(Pos.CENTER);
                allUsers.getChildren().add(userStackPane);

                // Make the user clickable
                userStackPane.setOnMouseClicked(event -> {
                    UserProfilePage userProfilePage = new UserProfilePage(stage, userID,
                            SQLiteOperations.INSTANCE.get_user_id(username));
                    userProfilePage.load();
                });

                // Change cursor to hand on hover
                userStackPane.setOnMouseEntered(event -> userStackPane.setStyle("-fx-cursor: hand;"));
                userStackPane.setOnMouseExited(event -> userStackPane.setStyle("-fx-cursor: default;"));
            }

        }

        // Set allUsers VBox as the content of the userBox
        userBox.setContent(allUsers);
        userBox.setFitToWidth(true);

        // Create VBox containers for padding of scroll box
        HBox scrollOfDayHBox = new HBox(10, scrollOfDayLabel, sodLegendImageView);
        HBox belowScrollBox = new HBox(130, scrollOfDayHBox, addScrollButton);
        HBox belowBelowScrollBox = new HBox(5, passwordField, resultOutput, setSodButton);
        VBox centerVBox = new VBox(5, scrollBox, belowScrollBox, belowBelowScrollBox);
        VBox leftVBox = new VBox(10, searchLabel, searchComboBox, searchTextField, searchButton);
        VBox rightVBox = new VBox(2, userBox, addUserButton, uploadsLabel, downloadsLabel);
        leftVBox.setPrefWidth(200);
        rightVBox.setPrefWidth(200);
        HBox.setMargin(passwordField, new Insets(0, 0, 0, 2));
        HBox.setMargin(resultOutput, new Insets(5, 0, 0, 0));
        VBox.setMargin(addUserButton, new Insets(0, 0, 0, 147));
        VBox.setMargin(uploadsLabel, new Insets(18, 0, 0, 0));
        leftVBox.setAlignment(Pos.TOP_CENTER);

        // Store the scroll box + left and right vboxes in a HBox
        HBox scrollContainer = new HBox(10, leftVBox, centerVBox, rightVBox);
        scrollContainer.setAlignment(Pos.CENTER);

        // Create containers
        VBox userDetailsVBox;
        if(!userID.equals("guest"))
            userDetailsVBox = new VBox(2, userIDLabel, userTypeLabel, viewProfileButton, logoutButton);
        else
            userDetailsVBox = new VBox(2, userIDLabel, userTypeLabel, logoutButton);
        HBox titleHBox = new HBox(190, title, usersTitle);
        VBox topVBox = new VBox(10, userDetailsVBox, titleHBox);
        HBox bottomHBox = new HBox(10, scrollViewArea, finalizeButton);
        userDetailsVBox.setPrefHeight(50);
        topVBox.setPrefHeight(135);
        titleHBox.setPrefHeight(65);
        bottomHBox.setPrefHeight(200);
        titleHBox.setAlignment(Pos.CENTER_RIGHT); // Align title to the center
        bottomHBox.setAlignment(Pos.CENTER); // Align scrollViewArea to the center
        titleHBox.setPadding(new Insets(0, 83, 0, 0));
        HBox.setMargin(scrollViewArea, new Insets(0, 0, 0, 90));
        HBox.setMargin(finalizeButton, new Insets(0, 0, 0, 0));

        if(userID.equals("guest"))
            bottomHBox.setPadding(new Insets(0, 0, 50, 0));
        else
            bottomHBox.setPadding(new Insets(0, 0, 68, 0));

        // Create a BorderPane for the entire page and add everything to it
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topVBox);
        borderPane.setCenter(scrollContainer);
        borderPane.setBottom(bottomHBox);

        // Create the scene
        Scene scene = new Scene(borderPane, 823, 584);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);

        EventHandler<ActionEvent> searchOptionHandler = event -> {
            if(searchComboBox.getValue().equals("Upload Date"))
                searchTextField.setPromptText("dd/mm/yyyy");
        };

        searchComboBox.setOnAction(searchOptionHandler);

        EventHandler<ActionEvent> searchHandler = event -> {
            if(searchComboBox.getValue() == null || searchTextField.getText().isEmpty()) {
                this.load(null);
            } else {
                String key = "";

                if (searchComboBox.getValue().equals("Title"))
                    key = "title";
                else if (searchComboBox.getValue().equals("Creator ID"))
                    key = "creator_id";
                else if (searchComboBox.getValue().equals("Scroll ID"))
                    key = "scroll_id";
                else if (searchComboBox.getValue().equals("Upload Date"))
                    key = "date";
                else
                    this.load(null);

                this.load(SQLiteOperations.INSTANCE.get_scroll_titles_filter(key, searchTextField.getText()));
            }
        };

        searchTextField.setOnAction(searchHandler);
        searchButton.setOnAction(searchHandler);

        EventHandler<ActionEvent> viewScrollHandler = event -> {
            if(SQLiteOperations.INSTANCE.access_scroll_psw(currentScroll, passwordField.getText()) == 1) {
                scrollViewArea.setText(SQLiteOperations.INSTANCE.get_scroll_content(currentScroll));
                resultOutput.setText("");
                passwordField.setText("");
            } else
                resultOutput.setText("Incorrect Password!");
        };

        passwordField.setOnAction(viewScrollHandler);

        EventHandler<ActionEvent> setSodHandler = event -> {
            SQLiteOperations.INSTANCE.set_scroll_day(currentScroll);
            this.load(null);
        };

        setSodButton.setOnAction(setSodHandler);

        viewProfileButton.setOnAction(actionEvent ->  {
            UserProfilePage userProfilePage = new UserProfilePage(stage, userID, userID);
            userProfilePage.load();
        });

        addUserButton.setOnAction(actionEvent ->  {
            AddUserPage addUserPage = new AddUserPage(stage, userID);
            addUserPage.load();
        });

        logoutButton.setOnAction(event -> {
            MainPage mainPage = new MainPage(stage);
            mainPage.load();
        });

        addScrollButton.setOnAction(e -> {
            AddScrollPage addScrollPage = new AddScrollPage(stage, userID);
            addScrollPage.load();
        });

        finalizeButton.setOnAction(event ->
                SQLiteOperations.INSTANCE.update_scroll_content(currentScroll, scrollViewArea.getText()));
    }

    public void downloadScroll(String title, Stage stage) {
        byte[] binaryData = SQLiteOperations.INSTANCE.get_scroll_content_as_bytes(title);

        // Open a FileChooser window to let the user save the file in a location of their choosing
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Scroll As");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary Files", "*.bin"));
        fileChooser.setInitialFileName(title + ".bin");

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(binaryData);
                fos.flush();
                SQLiteOperations.INSTANCE.increment_downloads(title, userID);
            } catch (IOException e) {
                System.out.println("Error downloading file: " + e.getMessage());
            }
        } else {
            System.out.println("File download was cancelled.");
        }
    }
}
