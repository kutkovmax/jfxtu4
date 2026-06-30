package ru.kutkovmax.jfxtu4;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import ru.kutkovmax.jfxtu4.controller.MachineController;
import ru.kutkovmax.jfxtu4.model.Tape;
import ru.kutkovmax.jfxtu4.view.TapeView;

import java.util.*;

public class Main extends Application {
    private static final String APP_NAME = "Эмулятор машины Тьюринга в четвёрках";

    private static final String VERSION = "0.1.0";

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {

//        List<Instruction> instrList = new ArrayList<Instruction>();
//        Map<String, Integer> stateToId = new HashMap<>();

        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.ttf"), 13);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.ttf"), 18);
        Font.loadFont(getClass().getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf"), 14);


        Text title = new Text(APP_NAME);
        title.getStyleClass().add("title");
        Button helpButton = new Button("?");
        HBox titleBox = new HBox(title,helpButton);
        Tape tape = new Tape("");
        TapeView tapeView = new TapeView(tape);


        Button startButton = new Button("Старт");
        Button backToEditButton = new Button("Вернуться к правке");
        Button quickButton = new Button("Быстро");
        Button stepButton = new Button("Шаг");
        startButton.managedProperty().bind(startButton.visibleProperty());
        backToEditButton.managedProperty().bind(backToEditButton.visibleProperty());
        quickButton.managedProperty().bind(quickButton.visibleProperty());
        stepButton.managedProperty().bind(stepButton.visibleProperty());
        stepButton.setVisible(false);
        quickButton.setVisible(false);
        backToEditButton.setVisible(false);
        HBox controlBox = new HBox(backToEditButton, quickButton, stepButton, startButton);
        controlBox.getStyleClass().add("control-panel");
        controlBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        TextArea programInput = new TextArea();
        VBox mainContentBox = new VBox(titleBox, tapeView, controlBox, programInput);

        Scene scene = new Scene(mainContentBox);
        String css = Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm();
        scene.getStylesheets().add(css);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app-icon.png"))));

        new MachineController(
                tapeView,
                programInput,
                startButton,
                backToEditButton,
                quickButton,
                stepButton
        );

        stage.setTitle(APP_NAME + " v" + VERSION);
        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        stage.show();
    }

}