package ru.kutkovmax.jfxtu4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ru.kutkovmax.jfxtu4.model.*;
import ru.kutkovmax.jfxtu4.view.TapeView;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MachineController {

    private static final String VERSION = "0.1.0";

    @FXML private Label appTitleLabel;
    @FXML private Label tapeLabel;
    @FXML private Label programLabel;

    @FXML private Button langButton;
    @FXML private Button helpButton;

    @FXML private StackPane tapeContainer;
    @FXML private TextArea programInput;
    @FXML private Button startButton;
    @FXML private Button backToEditButton;
    @FXML private Button quickButton;
    @FXML private Button stepButton;
    @FXML private Label statusLabel;

    private TapeView tapeView;
    private TuringMachine machine;

    private Locale currentLocale = new Locale("ru");
    private ResourceBundle bundle;

    private String currentStatusKey;
    private Object[] currentStatusArgs;

    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("messages", currentLocale);

        Tape tape = new Tape("");
        this.tapeView = new TapeView(tape);
        this.tapeContainer.getChildren().add(tapeView);
        updateTexts();
        initHandlers();
    }

    private void initHandlers() {
        startButton.setOnAction(event -> handleStart());
        stepButton.setOnAction(event -> handleStep());
        quickButton.setOnAction(event -> handleQuickRun());
        backToEditButton.setOnAction(event -> handleBackToEdit());
        langButton.setOnAction(event -> toggleLanguage());
    }

    private void toggleLanguage() {
        currentLocale = "ru".equals(currentLocale.getLanguage()) ? new Locale("en") : new Locale("ru");
        bundle = ResourceBundle.getBundle("messages", currentLocale);

        updateTexts();
    }

    private void updateTexts() {
        if (appTitleLabel != null) appTitleLabel.setText(bundle.getString("app.title"));
        if (tapeLabel != null) tapeLabel.setText(bundle.getString("label.tape"));
        if (programLabel != null) programLabel.setText(bundle.getString("label.program"));

        startButton.setText(bundle.getString("btn.start"));
        backToEditButton.setText(bundle.getString("btn.back"));
        quickButton.setText(bundle.getString("btn.quick"));
        stepButton.setText(bundle.getString("btn.step"));

        langButton.setText("ru".equals(currentLocale.getLanguage()) ? "RU" : "EN");

        if (langButton.getScene() != null && langButton.getScene().getWindow() != null) {
            Stage stage = (Stage) langButton.getScene().getWindow();
            stage.setTitle(bundle.getString("app.title") + " v" + VERSION);
        }

        if (currentStatusKey != null) {
            setStatus(currentStatusKey, currentStatusArgs);
        }
    }

    private void handleStart() {
        clearStatus();
        String programText = programInput.getText();
        try {
            Program program = ProgramParser.parse(programText);
            tapeView.getTape().moveHeadToEndOfInput();
            this.machine = new TuringMachine(tapeView.getTape(), program);

            tapeView.setEditable(false);

            startButton.setVisible(false);
            stepButton.setVisible(true);
            quickButton.setVisible(true);
            backToEditButton.setVisible(true);
        } catch (ParsingException e) {
            setStatus(e);
        }
    }

    private void handleStep() {
        try {
            machine.step();
            tapeView.renderTape();
            if (machine.isStalled()) {
                setStatus("info.finished");
                handleBackToEdit();
            }
        } catch (MachineException e) {
            setStatus(e.getType().getKey(), e.getArgs());
            handleBackToEdit();
        }
    }

    private void handleQuickRun() {
        try {
            while (!machine.isStalled()) {
                machine.step();
                tapeView.renderTape();
            }
            setStatus("info.finished");
            handleBackToEdit();
        } catch (MachineException e) {
            setStatus(e.getType().getKey(), e.getArgs());
            handleBackToEdit();
        }
    }

    private void handleBackToEdit() {
        startButton.setVisible(true);
        stepButton.setVisible(false);
        quickButton.setVisible(false);
        backToEditButton.setVisible(false);

        tapeView.setEditable(true);
    }

    private void setStatus(String key, Object... args) {
        this.currentStatusKey = key;
        this.currentStatusArgs = args;

        String pattern = bundle.getString(key);
        String text = MessageFormat.format(pattern, args);
        statusLabel.setText(text);
        statusLabel.getStyleClass().removeAll("status-error", "status-warning", "status-info");

        if (key.startsWith("error")) {
            statusLabel.getStyleClass().add("status-error");
        } else if (key.startsWith("warning")) {
            statusLabel.getStyleClass().add("status-warning");
        } else if (key.startsWith("info")) {
            statusLabel.getStyleClass().add("status-info");
        }
    }

    private void setStatus(ParsingException e) {
        setStatus(e.getType().getKey(), e.getArgs());
    }

    private void clearStatus() {
        this.currentStatusKey = null;
        this.currentStatusArgs = null;
        statusLabel.setText("");
        statusLabel.getStyleClass().removeAll("status-error", "status-warning", "status-info");
    }
}