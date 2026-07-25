package ru.kutkovmax.jfxtu4.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

    @FXML private VBox mainContent;
    @FXML private ScrollPane helpContainer;


    @FXML private Hyperlink githubLink;
    @FXML private Hyperlink inspiredLink;
    @FXML private Label helpMainTitle;
    @FXML private Label helpAboutHeader;
    @FXML private Label helpAboutText;
    @FXML private Label helpAuthor;
    @FXML private Label helpSource;
    @FXML private Label helpInspired;
    @FXML private Label helpManualTitle;
    @FXML private Label helpManualFormat;
    @FXML private Label helpManualQ;
    @FXML private Label helpManualA;
    @FXML private Label helpManualV;
    @FXML private Label helpManualQP;
    @FXML private Label helpManualCmdRight;
    @FXML private Label helpManualCmdLeft;
    @FXML private Label helpManualCmdStay;
    @FXML private Label helpManualCmdHalt;


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

    private boolean isHelpVisible = false;

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
        helpButton.setOnAction(event -> toggleHelp());
        githubLink.setOnAction(event -> openUrl("https://github.com/kutkovmax/jfxtu4"));
        inspiredLink.setOnAction(event -> openUrl("https://github.com/lxyd/jstu4"));
    }

    private void toggleHelp() {
        isHelpVisible = !isHelpVisible;

        mainContent.setVisible(!isHelpVisible);
        mainContent.setManaged(!isHelpVisible);

        helpContainer.setVisible(isHelpVisible);

        helpButton.setText(isHelpVisible ? "✕" : "?");
    }

    private void toggleLanguage() {
        currentLocale = "ru".equals(currentLocale.getLanguage()) ? new Locale("en") : new Locale("ru");
        bundle = ResourceBundle.getBundle("messages", currentLocale);

        updateTexts();
    }

    private void handleExecutionFinished() {
        Tape tape = tapeView.getTape();
        if (!tape.isHeadAfterResult()) {
            setStatus("warning.misposition");
        }else if (!tape.isInputPreserved()) {
            setStatus("warning.source_altered");
        } else {
            setStatus("info.finished");
        }
    }

    private void updateTexts() {
        if (appTitleLabel != null) appTitleLabel.setText(bundle.getString("app.title"));

        startButton.setText(bundle.getString("btn.start"));
        backToEditButton.setText(bundle.getString("btn.back"));
        quickButton.setText(bundle.getString("btn.quick"));
        stepButton.setText(bundle.getString("btn.step"));

        langButton.setText("ru".equals(currentLocale.getLanguage()) ? "RU" : "EN");

        helpMainTitle.setText(bundle.getString("help.title"));
        helpAboutHeader.setText(bundle.getString("help.about.title"));
        helpAboutText.setText(bundle.getString("help.about.text"));
        helpAuthor.setText(bundle.getString("help.author"));
        helpSource.setText(bundle.getString("help.source"));
        helpInspired.setText(bundle.getString("help.inspired"));
        helpManualTitle.setText(bundle.getString("help.manual.title"));
        helpManualFormat.setText(bundle.getString("help.manual.format"));
        helpManualQ.setText(bundle.getString("help.manual.q"));
        helpManualA.setText(bundle.getString("help.manual.a"));
        helpManualV.setText(bundle.getString("help.manual.v"));
        helpManualQP.setText(bundle.getString("help.manual.qp"));
        helpManualCmdRight.setText(bundle.getString("help.manual.cmd.right"));
        helpManualCmdLeft.setText(bundle.getString("help.manual.cmd.left"));
        helpManualCmdStay.setText(bundle.getString("help.manual.cmd.stay"));
        helpManualCmdHalt.setText(bundle.getString("help.manual.cmd.halt"));

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
                handleExecutionFinished();
            }
        } catch (MachineException e) {
            setStatus(e.getType().getKey(), e.getArgs());
        }
    }

    private void handleQuickRun() {
        try {
            while (!machine.isStalled()) {
                machine.step();
                tapeView.renderTape();
            }
            handleExecutionFinished();
        } catch (MachineException e) {
            setStatus(e.getType().getKey(), e.getArgs());
        }
    }

    private void handleBackToEdit() {
        startButton.setVisible(true);
        stepButton.setVisible(false);
        quickButton.setVisible(false);
        backToEditButton.setVisible(false);

        tapeView.getTape().restoreInput();
        tapeView.renderTape();

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

    private void openUrl(String url) {
        if (ru.kutkovmax.jfxtu4.Main.hostServices != null) {
            ru.kutkovmax.jfxtu4.Main.hostServices.showDocument(url);
        }
    }
}