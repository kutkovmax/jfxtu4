package ru.kutkovmax.jfxtu4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

public class Main extends Application {

    private static final String VERSION = "0.1.0";

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        loadFonts();

        ResourceBundle bundle = ResourceBundle.getBundle("messages");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"), bundle);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());

        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))));
        stage.setTitle(bundle.getString("app.title") + " v" + VERSION);
        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        stage.show();
    }

    private void loadFonts() {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.ttf"), 13);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.ttf"), 18);
        Font.loadFont(getClass().getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf"), 14);
    }
}