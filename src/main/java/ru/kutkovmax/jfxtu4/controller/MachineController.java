package ru.kutkovmax.jfxtu4.controller;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import ru.kutkovmax.jfxtu4.model.*;
import ru.kutkovmax.jfxtu4.view.TapeView;

public class MachineController {

    private final ResourceBundle bundle = ResourceBundle.getBundle("messages");

    private final TapeView tapeView;
    private final TextArea programInput;
    private final Button startButton;
    private final Button backToEditButton;
    private final Button quickButton;
    private final Button stepButton;
    private final Label statusLabel;

    private TuringMachine machine;

    public MachineController(TapeView tapeView, TextArea programInput,
                             Button startButton, Button backToEditButton,
                             Button quickButton, Button stepButton,
                             Label statusLabel) {
        this.tapeView = tapeView;
        this.programInput = programInput;
        this.startButton = startButton;
        this.backToEditButton = backToEditButton;
        this.quickButton = quickButton;
        this.stepButton = stepButton;
        this.statusLabel = statusLabel;

        initHandlers();
    }

    private void initHandlers() {
        startButton.setOnAction(event -> handleStart());
        stepButton.setOnAction(event -> handleStep());
        quickButton.setOnAction(event -> handleQuickRun());
        backToEditButton.setOnAction(event -> handleBackToEdit());
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
        statusLabel.setText("");
        statusLabel.getStyleClass().removeAll("status-error", "status-warning", "status-info");
    }
}