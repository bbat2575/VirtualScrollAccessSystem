package VirtualScrollAccessSystem;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AddScrollPage {
    private Stage stage;
    private String userID;
    private TextField titleField;
    private TextField filePathField;
    private PasswordField passwordField;
    private Label resultOutput;

    public AddScrollPage(Stage stage, String userID) {
        this.stage = stage;
        this.userID = userID;
        resultOutput = new Label("");
        resultOutput.setId("resultOutput");
    }

    public void load() {
        // Create page title
        Label title = new Label("Add Scroll");
        title.setId("title");

        // Create user details and logout/go back buttons
        Label userIDLabel = new Label("User ID: " + userID);
        Label userTypeLabel = new Label("User Type: ");
        Button viewProfileButton = new Button("View Profile");
        Button logoutButton = new Button("Logout");
        Button goBackButton = new Button("Go Back");

        // Set label for admin, user and guest
        if(userID.equals("admin"))
            userTypeLabel.setText(userTypeLabel.getText() + "Admin");
        else if(userID.equals("guest"))
            userTypeLabel.setText(userTypeLabel.getText() + "Guest");
        else
            userTypeLabel.setText(userTypeLabel.getText() + "User");


        // Create labels and text fields
        Label titleLabel = new Label("Scroll Title:");
        titleField = new TextField();

        Label fileLabel = new Label("File Data:");
        filePathField = new TextField();
        filePathField.setEditable(false); // Users shouldn't type here directly

        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();

        // Create buttons
        Button browseButton = new Button("Browse");
        browseButton.setOnAction(e -> chooseFile());
        Button addScrollButton = new Button("Add Scroll");

        // Form Layout
        GridPane addScrollGrid = new GridPane();
        addScrollGrid.setAlignment(Pos.CENTER);
        addScrollGrid.setHgap(10);
        addScrollGrid.setVgap(10);
        addScrollGrid.setPadding(new Insets(25, 25, 25, 25));

        // Adding nodes to the addScrollGrid
        addScrollGrid.add(titleLabel, 0, 0);
        addScrollGrid.add(titleField, 1, 0);

        addScrollGrid.add(fileLabel, 0, 1);
        addScrollGrid.add(filePathField, 1, 1);
        addScrollGrid.add(browseButton, 2, 1);

        addScrollGrid.add(passwordLabel, 0, 2);
        addScrollGrid.add(passwordField, 1, 2);
        passwordField.setPromptText("Optional");

        VBox addScrollVBox = new VBox(15, addScrollGrid, addScrollButton);
        addScrollVBox.setAlignment(Pos.CENTER);

        // Create VBox containers for padding of scroll box
        VBox leftVBox = new VBox();
        VBox rightVBox = new VBox();
        leftVBox.setPrefWidth(200);
        rightVBox.setPrefWidth(200);

        // Store the add scroll form + left and right vboxes in a HBox
        HBox addScrollContainer = new HBox(10, leftVBox, addScrollVBox, rightVBox);
        addScrollContainer.setAlignment(Pos.CENTER);

        // Create containers
        HBox navigationHBox = new HBox(2, logoutButton, goBackButton);
        VBox userDetailsVBox = new VBox(2, userIDLabel, userTypeLabel, viewProfileButton, navigationHBox);
        HBox titleHBox = new HBox(10, title);
        VBox topVBox = new VBox(10, userDetailsVBox, titleHBox);
        HBox bottomHBox = new HBox(10, resultOutput);
        userDetailsVBox.setPrefHeight(50);
        titleHBox.setPrefHeight(65);
        bottomHBox.setPrefHeight(200);
        titleHBox.setAlignment(Pos.CENTER); // Align title to the center
        bottomHBox.setAlignment(Pos.CENTER); // Align resultOutput to the center
        HBox.setMargin(resultOutput, new Insets(30, 0, 0, 0));

        // Create a BorderPane for the entire page and add everything to it
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topVBox);
        borderPane.setCenter(addScrollContainer);
        borderPane.setBottom(bottomHBox);

        // Create the scene
        Scene scene = new Scene(borderPane, 823, 584);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);

        EventHandler<ActionEvent> addScrollHandler = event -> {
            addScroll();
        };

        addScrollButton.setOnAction(addScrollHandler);
        titleField.setOnAction(addScrollHandler);
        passwordField.setOnAction(addScrollHandler);

        viewProfileButton.setOnAction(actionEvent ->  {
            UserProfilePage userProfilePage = new UserProfilePage(stage, userID, userID);
            userProfilePage.load();
        });

        logoutButton.setOnAction(event -> {
            MainPage mainPage = new MainPage(stage);
            mainPage.load();
        });

        goBackButton.setOnAction(e -> {
            ViewScrollsPage viewScrollsPage = new ViewScrollsPage(stage, userID);
            viewScrollsPage.load(null);
        });
    }

    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File Data");
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void addScroll() {
        String title = titleField.getText();
        String filePath = filePathField.getText();

        if (title.isEmpty() || filePath.isEmpty()) {
            resultOutput.setText("A title and file path are required!");
            return;
        }

        if (SQLiteOperations.INSTANCE.get_scroll_titles().contains(title)) {
            resultOutput.setText("Scroll Title already exists! Please choose another.");
            return;
        }

        // Get current date and time in Sydney
        ZoneId zoneId = ZoneId.of("Australia/Sydney");
        LocalDateTime now = LocalDateTime.now(zoneId);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String date = now.format(dateFormatter);

        // Add the scroll to the db
        int result = SQLiteOperations.INSTANCE.insert_scroll(title, userID, filePath, date, passwordField.getText());

        if (result == 1) {
            resultOutput.setText("Scroll added successfully.");
        } else {
            resultOutput.setText("Error adding scroll.");
        }
    }
}
