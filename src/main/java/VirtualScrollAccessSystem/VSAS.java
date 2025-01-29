package VirtualScrollAccessSystem;

import javafx.application.Application;
import javafx.stage.Stage;

public class VSAS extends Application {
    @Override
    public void start(Stage stage) {
        // Set the window size
        stage.setWidth(823);
        stage.setHeight(584);
        stage.setMinWidth(823);
        stage.setMinHeight(584);
        stage.setMaxWidth(823);
        stage.setMaxHeight(584);

        // Set the window title and show it
        stage.setTitle("Virtual Scroll Access System");
        stage.show();

        // Load the Main Page
        MainPage mainPage = new MainPage(stage);
        mainPage.load();
    }

    public static void main(String[] args) {
        SQLiteConnection.INSTANCE.connect();
        SQLiteConnection.INSTANCE.initializeTables();

        launch();
    }
}
