package ru.kutkovmax.jfxtu4.controller;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import ru.kutkovmax.jfxtu4.Main;
import ru.kutkovmax.jfxtu4.model.*;
import ru.kutkovmax.jfxtu4.view.TapeView;

import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MachineController {

    @FXML private VBox rootBox;
    @FXML private Label appTitleLabel;

    @FXML private Button langButton;
    @FXML private Button helpButton;
    @FXML private Button openButton;
    @FXML private Button saveButton;

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

    @FXML private Label helpHotkeysTitle;
    @FXML private Label helpHotkeysEditRun;
    @FXML private Label helpHotkeysEditTapeEnter;
    @FXML private Label helpHotkeysRunToggleQuick;
    @FXML private Label helpHotkeysRunStep;
    @FXML private Label helpHotkeysRunStop;

    @FXML private Label helpExampleTitle;
    @FXML private Label helpExampleText;


    @FXML private StackPane tapeContainer;
    @FXML private TextArea programInput;
    @FXML private Button startButton;
    @FXML private Button backToEditButton;
    @FXML private Button quickButton;
    @FXML private Button instantButton;
    @FXML private Button stepButton;
    @FXML private Label statusLabel;

    private TapeView tapeView;
    private TuringMachine machine;

    private Locale currentLocale = new Locale("ru");
    private ResourceBundle bundle;

    private String currentStatusKey;
    private Object[] currentStatusArgs;

    private boolean isHelpVisible = false;

    private Timeline quickTimeline;
    private Thread instantRunThread;
    private volatile boolean instantRunCancelled;

    private final EventHandler<MouseEvent> consumeMouseHandler = MouseEvent::consume;

    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("messages", currentLocale);

        Tape tape = new Tape("");

        this.tapeView = new TapeView(tape);
        this.tapeView.widthProperty().bind(tapeContainer.widthProperty());
        this.tapeView.heightProperty().bind(tapeContainer.heightProperty());
        this.tapeContainer.getChildren().add(tapeView);

        programInput.getStyleClass().add("edit-mode");

        rootBox.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPressed);

        updateTexts();
        initHandlers();
    }

    private boolean isInRunMode() {
        return !startButton.isVisible();
    }

    private void handleGlobalKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        boolean ctrl = event.isControlDown() || event.isShortcutDown();

        if (isInRunMode()) {
            if (code == KeyCode.ESCAPE) {
                handleBackToEdit();
                event.consume();
                return;
            }
            if (code == KeyCode.SPACE) {
                handleStep();
                event.consume();
                return;
            }
            if (code == KeyCode.ENTER) {
                handleQuickRun();
                event.consume();
                return;
            }
        } else {
            if (ctrl && code == KeyCode.ENTER) {
                if (startButton.isVisible()) {
                    handleStart();
                    event.consume();
                }
            }
        }
    }

    private void initHandlers() {
        rootBox.setFocusTraversable(true);
        rootBox.setOnMousePressed(event -> {
            rootBox.requestFocus();
        });
        openButton.setOnAction(event -> handleOpen());
        saveButton.setOnAction(event -> handleSave());
        startButton.setOnAction(event -> handleStart());
        stepButton.setOnAction(event -> handleStep());
        quickButton.setOnAction(event -> handleQuickRun());
        instantButton.setOnAction(event -> handleInstantRun());
        backToEditButton.setOnAction(event -> handleBackToEdit());
        langButton.setOnAction(event -> toggleLanguage());
        helpButton.setOnAction(event -> toggleHelp());
        githubLink.setOnAction(event -> openUrl("https://github.com/kutkovmax/jfxtu4"));
        inspiredLink.setOnAction(event -> openUrl("https://github.com/lxyd/jstu4"));

        tapeView.setOnRunAction(this::handleStart);
        tapeView.setOnToggleFastModeAction(this::handleQuickRun);
        tapeView.setOnStepAction(this::handleStep);
        tapeView.setOnStopAction(this::handleBackToEdit);

        programInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                if (startButton.isVisible()) {
                    handleStart();
                    event.consume();
                }
            }
        });
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
        enterStalledState();
    }

    private void enterStalledState() {
        stepButton.setVisible(false);
        quickButton.setVisible(false);
        instantButton.setVisible(false);
        backToEditButton.setVisible(true);
        programInput.deselect();
    }

    private void updateTexts() {
        if (appTitleLabel != null) appTitleLabel.setText(bundle.getString("app.title"));

        openButton.setText(bundle.getString("btn.open"));
        saveButton.setText(bundle.getString("btn.save"));

        startButton.setText(bundle.getString("btn.start"));
        backToEditButton.setText(bundle.getString("btn.back"));
        quickButton.setText(bundle.getString("btn.quick"));
        instantButton.setText(bundle.getString("btn.instant"));
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

        helpHotkeysTitle.setText(bundle.getString("help.hotkeys.title"));
        helpHotkeysEditRun.setText(bundle.getString("help.hotkeys.edit.run"));
        helpHotkeysEditTapeEnter.setText(bundle.getString("help.hotkeys.edit.tape.enter"));
        helpHotkeysRunToggleQuick.setText(bundle.getString("help.hotkeys.run.toggle_quick"));
        helpHotkeysRunStep.setText(bundle.getString("help.hotkeys.run.step"));
        helpHotkeysRunStop.setText(bundle.getString("help.hotkeys.run.stop"));

        helpExampleTitle.setText(bundle.getString("help.example.title"));
        helpExampleText.setText(bundle.getString("help.example.text"));

        if (langButton.getScene() != null && langButton.getScene().getWindow() != null) {
            Stage stage = (Stage) langButton.getScene().getWindow();
            stage.setTitle(bundle.getString("app.title") + " v" + Main.VERSION);
        }

        if (currentStatusKey != null) {
            setStatus(currentStatusKey, currentStatusArgs);
        }
    }

    private void handleStart() {
        stopExecution();
        clearStatus();
        String programText = programInput.getText();
        try {
            Program program = ProgramParser.parse(programText);
            tapeView.getTape().moveHeadToEndOfInput();
            this.machine = new TuringMachine(tapeView.getTape(), program);

            tapeView.setEditable(false);
            programInput.setEditable(false);

            programInput.getStyleClass().remove("edit-mode");
            programInput.getStyleClass().add("run-mode");

            programInput.addEventFilter(MouseEvent.MOUSE_PRESSED, consumeMouseHandler);
            programInput.addEventFilter(MouseEvent.MOUSE_DRAGGED, consumeMouseHandler);
            programInput.addEventFilter(MouseEvent.MOUSE_RELEASED, consumeMouseHandler);

            startButton.setVisible(false);
            stepButton.setVisible(true);
            quickButton.setVisible(true);
            instantButton.setVisible(true);
            backToEditButton.setVisible(true);
            highlightLine(machine.getNextLineIndex());
        } catch (ParsingException e) {
            setStatus(e);
        }
    }

    private void handleStep() {
        if (machine == null || machine.isStalled()) {
            return;
        }
        stopExecution();
        try {
            machine.step();
            tapeView.renderTape();
            if (machine.isStalled()) {
                programInput.deselect();
                handleExecutionFinished();
            }else {
                highlightLine(machine.getNextLineIndex());
            }
        } catch (MachineException e) {
            setStatus(e.getType().getKey(), e.getArgs());
            enterStalledState();
        }
    }

    private void handleQuickRun() {
        if (machine == null || machine.isStalled()) {
            return;
        }
        if (quickTimeline != null && quickTimeline.getStatus() == Animation.Status.RUNNING) {
            stopExecution();
            return;
        }
        stopInstantRun();

        quickTimeline = new Timeline(new KeyFrame(Duration.millis(80), event -> {
            try {
                machine.step();
                tapeView.renderTape();

                if (machine.isStalled()) {
                    stopQuickTimer();
                    programInput.deselect();
                    handleExecutionFinished();
                } else {
                    highlightLine(machine.getNextLineIndex());
                }
            } catch (MachineException e) {
                stopQuickTimer();
                setStatus(e.getType().getKey(), e.getArgs());
                enterStalledState();
            }
        }));

        quickTimeline.setCycleCount(Animation.INDEFINITE);
        quickTimeline.play();
    }


    private void stopQuickTimer() {
        if (quickTimeline != null) {
            quickTimeline.stop();
            quickTimeline = null;
        }
    }

    private void stopInstantRun() {
        instantRunCancelled = true;
        if (instantRunThread != null) {
            instantRunThread.interrupt();
            instantRunThread = null;
        }
    }

    private void stopExecution() {
        stopQuickTimer();
        stopInstantRun();
    }

    private void handleInstantRun() {
        if (machine == null || machine.isStalled()) {
            return;
        }
        stopExecution();

        instantRunCancelled = false;
        instantRunThread = new Thread(() -> {
            try {
                final long RENDER_INTERVAL_MS = 40L;
                long nextRenderAt = System.currentTimeMillis() + RENDER_INTERVAL_MS;
                boolean[] renderPending = {false};

                while (!machine.isStalled() && !instantRunCancelled) {
                    machine.step();

                    long now = System.currentTimeMillis();
                    if (now >= nextRenderAt && !renderPending[0]) {
                        nextRenderAt = now + RENDER_INTERVAL_MS;
                        final int lineIdx = machine.isStalled() ? -1 : machine.getNextLineIndex();
                        renderPending[0] = true;
                        Platform.runLater(() -> {
                            try {
                                if (!instantRunCancelled) {
                                    tapeView.renderTape();
                                    if (lineIdx >= 0) highlightLine(lineIdx);
                                }
                            } finally {
                                renderPending[0] = false;
                            }
                        });
                    }
                }
                if (!instantRunCancelled) {
                    Platform.runLater(() -> {
                        tapeView.renderTape();
                        programInput.deselect();
                        handleExecutionFinished();
                    });
                }
            } catch (MachineException e) {
                Platform.runLater(() -> {
                    tapeView.renderTape();
                    setStatus(e.getType().getKey(), e.getArgs());
                    enterStalledState();
                });
            }
        }, "Instant-Run-Thread");
        instantRunThread.setDaemon(true);
        instantRunThread.start();
    }

    private void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("dialog.save.title"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(bundle.getString("dialog.file.filter") + " (*.tu4)", "*.tu4"),
                new FileChooser.ExtensionFilter(bundle.getString("dialog.all.files"), "*.*")
        );
        fileChooser.setInitialFileName("program.tu4");

        Stage stage = (Stage) programInput.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            if (!file.getName().toLowerCase().endsWith(".tu4")) {
                file = new File(file.getAbsolutePath() + ".tu4");
            }
            try {
                java.nio.file.Files.writeString(file.toPath(), programInput.getText());
                setStatus("info.saved");
            } catch (Exception e) {
                setStatus("error.file_save");
            }
        }
    }

    private void handleOpen() {
        stopExecution();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("dialog.open.title"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(bundle.getString("dialog.file.filter") + " (*.tu4)", "*.tu4"),
                new FileChooser.ExtensionFilter(bundle.getString("dialog.all.files"), "*.*")
        );

        Stage stage = (Stage) programInput.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                String content = java.nio.file.Files.readString(file.toPath());
                programInput.setText(content);
                setStatus("info.loaded");
            } catch (Exception e) {
                setStatus("error.file_open");
            }
        }
    }

    private void handleBackToEdit() {
        stopExecution();
        clearStatus();

        startButton.setVisible(true);
        stepButton.setVisible(false);
        quickButton.setVisible(false);
        instantButton.setVisible(false);
        backToEditButton.setVisible(false);

        tapeView.getTape().restoreInput();
        tapeView.renderTape();
        programInput.deselect();
        programInput.setEditable(true);
        tapeView.setEditable(true);

        programInput.removeEventFilter(MouseEvent.MOUSE_PRESSED, consumeMouseHandler);
        programInput.removeEventFilter(MouseEvent.MOUSE_DRAGGED, consumeMouseHandler);
        programInput.removeEventFilter(MouseEvent.MOUSE_RELEASED, consumeMouseHandler);

        programInput.getStyleClass().remove("run-mode");
        programInput.getStyleClass().add("edit-mode");
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

    private void highlightLine(int lineIndex) {
        if (lineIndex < 0) {
            programInput.deselect();
            return;
        }

        String text = programInput.getText();
        String[] lines = text.split("\n", -1);
        if (lineIndex >= lines.length) return;

        int start = 0;
        for (int i = 0; i < lineIndex; i++) {
            start += lines[i].length() + 1;
        }
        int end = start + lines[lineIndex].length();

        programInput.setEditable(false);
        programInput.selectRange(start, end);
    }

    private void openUrl(String url) {
        if (Main.hostServices != null) {
            Main.hostServices.showDocument(url);
        }
    }
}
