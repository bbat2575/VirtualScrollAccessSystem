package VirtualScrollAccessSystem;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;


public class SignInPage {
    private Stage stage;

    public SignInPage(Stage stage) {
        this.stage = stage;
    }

    public void load() {
        // Create page title, labels, input fields and button
        Label title = new Label("Sign In");
        Label usernameLabel = new Label("Username: ");
        Label passwordLabel = new Label("Password: ");
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button logInButton = new Button("Log-in");
        Button backButton = new Button("Back");
        Label resultOutput = new Label("");

        // Apply ID for CSS
        title.setId("title");
        resultOutput.setId("resultOutput");

        // Create a spacer to separate resultOutput from the other elements
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        spacer.setPrefHeight(100);

        // Create HBoxes for the username and password fields
        HBox usernameHBox = new HBox(10, usernameLabel, usernameField);
        usernameHBox.setAlignment(Pos.CENTER);
        HBox passwordHBox = new HBox(10, passwordLabel, passwordField);
        passwordHBox.setAlignment(Pos.CENTER);
        HBox buttonsHBox = new HBox(10, backButton, logInButton);
        buttonsHBox.setAlignment(Pos.CENTER);

        // Create VBox to place the above elements into
        VBox signInVBox = new VBox(10, title, usernameHBox, passwordHBox, buttonsHBox, spacer, resultOutput);
        signInVBox.setAlignment(Pos.CENTER);
        signInVBox.setPadding(new Insets(20));

        // Add margin adjustments
        VBox.setMargin(title, new Insets(180, 0, 10, 0));
        VBox.setMargin(usernameHBox, new Insets(0, 5, 0, 0));
        VBox.setMargin(buttonsHBox, new Insets(5, 0, 0, 10));
        VBox.setMargin(resultOutput, new Insets(10, 0, 0, 0));
        VBox.setMargin(resultOutput, new Insets(0, 0, 30, 0));

        // Create a basic borderpane layout for the page
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(signInVBox);

        // Create the scene
        Scene scene = new Scene(borderPane, 823, 584);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); // set stylesheet
        stage.setScene(scene);

        EventHandler<ActionEvent> signInHandler = event -> {
            if (usernameField.getText().isEmpty()) {
                resultOutput.setText("Please specify a username!");
            } else if (passwordField.getText().isEmpty()) {
                resultOutput.setText("Please specify a password!");
            } else {
                String userID = SQLiteOperations.INSTANCE.user_login(usernameField.getText(), passwordField.getText());

                if (userID != null) {
                    ViewScrollsPage scrollPage = new ViewScrollsPage(stage, userID);
                    scrollPage.load(null);
                } else {
                    resultOutput.setText("Password incorrect or user doesn't exist!");
                }
            }
        };

        usernameField.setOnAction(signInHandler);
        passwordField.setOnAction(signInHandler);
        logInButton.setOnAction(signInHandler);

        backButton.setOnAction(e -> {
            MainPage mainPage = new MainPage(stage);
            mainPage.load();
        });
    }
}
