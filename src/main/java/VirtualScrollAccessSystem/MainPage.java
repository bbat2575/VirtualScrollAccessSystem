package VirtualScrollAccessSystem;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.util.List;



public class MainPage {
    private Stage stage;

    public MainPage(Stage stage) {
        this.stage = stage;
    }

    public void load() {
        // Create main page title and buttons
        Label title = new Label("Virtual Scroll Access System");
        title.setId("title"); // Apply ID for CSS
        Button signIn = new Button("Sign-in");
        Button createAccount = new Button("Create account");
        Button guestSignIn = new Button("Sign-in as guest");



        // Create VBox to place the above elements into
        VBox mainVBox = new VBox(10, title, signIn, createAccount, guestSignIn);
        mainVBox.setAlignment(Pos.CENTER);
        VBox.setMargin(title, new Insets(0, 0, 10, 0));

        // Create a basic BorderPane layout for the page
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(mainVBox);

        // Create the scene
        Scene scene = new Scene(borderPane, 823, 584);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);

        // Load Sign-In Page when signIn button clicked
        signIn.setOnAction(e -> {
            SignInPage signInPage = new SignInPage(stage);
            signInPage.load();
        });

        // Load Create Account Page when createAccount button clicked
        createAccount.setOnAction(e -> {
            CreateAccountPage createAccountPage = new CreateAccountPage(stage);
            createAccountPage.load();
        });

        guestSignIn.setOnAction(e -> {
            ViewScrollsPage scrollPage = new ViewScrollsPage(stage, "guest");
            scrollPage.load(null);
        });
    }
}
