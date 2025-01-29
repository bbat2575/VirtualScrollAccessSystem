package VirtualScrollAccessSystem;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class UserProfilePage {
    private Stage stage;
    private String userID;
    // Store the username for the user profile being viewed
    private String profileUserID;

    public UserProfilePage(Stage stage, String userID, String profileUserID) {
        this.stage = stage;
        this.userID = userID;
        this.profileUserID = profileUserID;
    }

    public void load() {
        // Create page title, labels, input fields and buttons
        Label title = new Label("User Profile");
        title.setId("title");
        Label fullNameLabel = new Label("Full name: ");
        Label phoneLabel = new Label("Phone #: ");
        Label emailLabel = new Label("Email: ");
        Label idKeyLabel = new Label("ID key: ");
        TextField idKeyField = new TextField();
        idKeyField.setEditable(false); // ID is always uneditable since it's PK
        Label usernameLabel = new Label("Username: ");
        Label passwordLabel = new Label("Password: ");
        TextField fullNameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Label uploadsLabel = new Label("Uploads: " + SQLiteOperations.INSTANCE.get_user_uploads(profileUserID));
        Label downloadsLabel = new Label("Downloads: " + SQLiteOperations.INSTANCE.get_user_downloads(profileUserID));
        Button submitChangesButton = new Button("Finalize");
        Label resultOutput = new Label("");
        resultOutput.setId("resultOutput");

        if(!userID.equals(profileUserID)) {
            fullNameField.setEditable(false);
            phoneField.setEditable(false);
            emailField.setEditable(false);
            passwordField.setEditable(false);
            usernameField.setEditable(false);
            submitChangesButton.setVisible(false);
        }

        // Add values to the TextFields
        List<String> userDetails = SQLiteOperations.INSTANCE.get_user_details(profileUserID);
        fullNameField.setText(userDetails.get(2));
        phoneField.setText(userDetails.get(0));
        emailField.setText(userDetails.get(1));
        idKeyField.setText(profileUserID);
        usernameField.setText(userDetails.get(3));
        passwordField.setText(userDetails.get(4));

        // Create user details and logout/go back buttons
        Label userIDLabel = new Label("User ID: " + userID);
        Label userTypeLabel = new Label("User Type: ");
        Button viewProfileButton = new Button("View Profile");
        Button logoutButton = new Button("Logout");
        Button goBackButton = new Button("Go Back");

        // Set label for admin and user
        if(userID.equals("admin"))
            userTypeLabel.setText(userTypeLabel.getText() + "Admin");
        else
            userTypeLabel.setText(userTypeLabel.getText() + "User");

        // Create a spacer to separate resultOutput from the other elements
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        spacer.setPrefHeight(100);

        // Create details container
        HBox navigationHBox = new HBox(2, logoutButton, goBackButton);
        VBox userDetailsVBox = new VBox(2, userIDLabel, userTypeLabel, viewProfileButton, navigationHBox);
        HBox titleHBox = new HBox(10, title);
        titleHBox.setAlignment(Pos.CENTER);
        VBox topVBox = new VBox(10, userDetailsVBox, titleHBox);

        // Place Label/Textfield pairs (and Buttons) in HBoxes
        HBox fullNameHBox = new HBox(10, fullNameLabel, fullNameField, uploadsLabel);
        fullNameHBox.setAlignment(Pos.CENTER_RIGHT);
        HBox phoneHBox = new HBox(10, phoneLabel, phoneField, downloadsLabel);
        phoneHBox.setAlignment(Pos.CENTER_RIGHT);
        HBox emailHBox = new HBox(10, emailLabel, emailField);
        emailHBox.setAlignment(Pos.CENTER_RIGHT);
        HBox idKeyHbox = new HBox(10, idKeyLabel, idKeyField);
        idKeyHbox.setAlignment(Pos.CENTER_RIGHT);
        HBox usernameHBox = new HBox(10, usernameLabel, usernameField);
        usernameHBox.setAlignment(Pos.CENTER_RIGHT);
        HBox passwordHBox = new HBox(10, passwordLabel, passwordField);
        passwordHBox.setAlignment(Pos.CENTER_RIGHT);
        HBox buttonsHBox = new HBox(10, submitChangesButton);
        buttonsHBox.setAlignment(Pos.CENTER_RIGHT);
        fullNameHBox.setPadding(new Insets(0, 199, 0, 0));
        phoneHBox.setPadding(new Insets(0, 178, 0, 0));
        emailHBox.setPadding(new Insets(0, 285, 0, 0));
        idKeyHbox.setPadding(new Insets(0, 285, 0, 0));
        usernameHBox.setPadding(new Insets(0, 285, 0, 0));
        passwordHBox.setPadding(new Insets(0, 285, 0, 0));
        buttonsHBox.setPadding(new Insets(8, 356, 0, 0));

        // Combine everything into a single VBox
        VBox userProfileVBox = new VBox(10, fullNameHBox, phoneHBox, emailHBox, idKeyHbox, usernameHBox,
                passwordHBox, buttonsHBox, spacer, resultOutput);
        userProfileVBox.setPadding(new Insets(20, 0, 20, 0));
        userProfileVBox.setAlignment(Pos.CENTER);
        VBox.setMargin(resultOutput, new Insets(0, 0, 30, 0));

        // Create a basic borderpane layout for the page
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topVBox);
        borderPane.setCenter(userProfileVBox);

        // Create the scene
        Scene scene = new Scene(borderPane, 823, 584);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); // set stylesheet
        stage.setScene(scene);

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



        MainPage mainPage = new MainPage(stage);

        // Create a single EventHandler for submitting the form when enter key or button pressed
        EventHandler<ActionEvent> updateAccountHandler = event -> {
            if(usernameField.getText().isEmpty())
                resultOutput.setText("Username cannot be empty!");
            else if(passwordField.getText().isEmpty())
                resultOutput.setText("Password cannot be empty!");
            else{
                try {
                    int result = SQLiteOperations.INSTANCE.update_user_details(idKeyField.getText(), phoneField.getText(),
                            emailField.getText(), fullNameField.getText(), usernameField.getText(), passwordField.getText());

                    if(result == 0)
                        resultOutput.setText("Update successful.");
                    else if(result == -2) {
                        resultOutput.setText("Username already exists! Please choose another.");
                    } else
                        resultOutput.setText("Something went wrong!");
                } catch (SQLException e2) {
                    System.out.println("SQL Error");
                    resultOutput.setText("Something went wrong!.");
                }
            }
        };

        // Attach the EventHandler to the TextFields, PasswordField, and Button
        fullNameField.setOnAction(updateAccountHandler);
        phoneField.setOnAction(updateAccountHandler);
        emailField.setOnAction(updateAccountHandler);
        idKeyField.setOnAction(updateAccountHandler);
        usernameField.setOnAction(updateAccountHandler);
        passwordField.setOnAction(updateAccountHandler);
        submitChangesButton.setOnAction(updateAccountHandler);
    }
}
