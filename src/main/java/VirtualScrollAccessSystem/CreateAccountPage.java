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
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class CreateAccountPage {
    private Stage stage;

    public CreateAccountPage(Stage stage) {
        this.stage = stage;
    }

    public void load() {
        // Create page title, labels, input fields and buttons
        Label title = new Label("Create Account");
        Label fullNameLabel = new Label("Full name: ");
        Label phoneLabel = new Label("Phone #: ");
        Label emailLabel = new Label("Email: ");
        Label idKeyLabel = new Label("ID key: ");
        Label usernameLabel = new Label("Username: ");
        Label passwordLabel = new Label("Password: ");
        TextField fullNameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField idKeyField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button createAccountButton = new Button("Create Account");
        Button discardButton = new Button("Discard");
        Label resultOutput = new Label("");

        // Apply ID for CSS
        title.setId("title");
        resultOutput.setId("resultOutput");

        // Create a spacer to separate resultOutput from the other elements
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        spacer.setPrefHeight(100);

        // Place Label/Textfield pairs (and Buttons) in HBoxes
        HBox fullNameHBox = new HBox(10, fullNameLabel, fullNameField);
        fullNameHBox.setAlignment(Pos.CENTER);
        HBox phoneHBox = new HBox(10, phoneLabel, phoneField);
        phoneHBox.setAlignment(Pos.CENTER);
        HBox emailHBox = new HBox(10, emailLabel, emailField);
        emailHBox.setAlignment(Pos.CENTER);
        HBox idKeyHBox = new HBox(10, idKeyLabel, idKeyField);
        idKeyHBox.setAlignment(Pos.CENTER);
        HBox usernameHBox = new HBox(10, usernameLabel, usernameField);
        usernameHBox.setAlignment(Pos.CENTER);
        HBox passwordHBox = new HBox(10, passwordLabel, passwordField);
        passwordHBox.setAlignment(Pos.CENTER);
        HBox buttonsHBox = new HBox(10, discardButton, createAccountButton);
        buttonsHBox.setAlignment(Pos.CENTER);

        // Combine everything into a single VBox
        VBox createAccountVBox = new VBox(10, title, fullNameHBox, phoneHBox, emailHBox, idKeyHBox,
                usernameHBox, passwordHBox, buttonsHBox, spacer, resultOutput);
        createAccountVBox.setAlignment(Pos.CENTER);
        createAccountVBox.setPadding(new Insets(20));

        // Add margin adjustments
        VBox.setMargin(title, new Insets(100, 0, 10, 0));
        VBox.setMargin(phoneHBox, new Insets(0, 0, 0, 13));
        VBox.setMargin(emailHBox, new Insets(0, 0, 0, 30));
        VBox.setMargin(idKeyHBox, new Insets(0, 0, 0, 25));
        VBox.setMargin(passwordHBox, new Insets(0, 0, 0, 5));
        VBox.setMargin(buttonsHBox, new Insets(5, 0, 0, 23));
        VBox.setMargin(resultOutput, new Insets(0, 0, 30, 0));

        // Create a basic borderpane layout for the page
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(createAccountVBox);

        // Create the scene
        Scene scene = new Scene(borderPane, 823, 584);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); // set stylesheet
        stage.setScene(scene);

        MainPage mainPage = new MainPage(stage);

        // Go back to Main Page if discard button clicked
        discardButton.setOnAction(e -> {
            mainPage.load();
        });

        // Create a single EventHandler for submitting the form when enter key or button pressed
        EventHandler<ActionEvent> createAccountHandler = event -> {
            if(fullNameField.getText().isEmpty())
                resultOutput.setText("Please specify your full name!");
            else if(phoneField.getText().isEmpty())
                resultOutput.setText("Please specify a phone number!");
            else if(emailField.getText().isEmpty())
                resultOutput.setText("Please specify an email!");
            else if(idKeyField.getText().isEmpty())
                resultOutput.setText("Please specify an ID!");
            else if(usernameField.getText().isEmpty())
                resultOutput.setText("Please specify a username!");
            else if(usernameField.getText().length() > 8)
                resultOutput.setText("Username cannot be longer than 8 characters!");
            else if(passwordField.getText().isEmpty())
                resultOutput.setText("Please specify a password!");
            else{
                try {
                    int result = SQLiteOperations.INSTANCE.insert_user(phoneField.getText(), emailField.getText(), fullNameField.getText(),
                            idKeyField.getText(), usernameField.getText(), passwordField.getText());

                    if(result == -1)
                        resultOutput.setText("ID key already exists! Please choose another.");
                    else if (result == -2)
                        resultOutput.setText("Username already exists! Please choose another.");
                    else
                        mainPage.load();
                } catch (SQLException e2) {
                    System.out.println("SQL Error");
                }
            }
        };

        // Attach the EventHandler to the TextFields, PasswordField, and Button
        fullNameField.setOnAction(createAccountHandler);
        phoneField.setOnAction(createAccountHandler);
        emailField.setOnAction(createAccountHandler);
        idKeyField.setOnAction(createAccountHandler);
        usernameField.setOnAction(createAccountHandler);
        passwordField.setOnAction(createAccountHandler);
        createAccountButton.setOnAction(createAccountHandler);
    }
}
