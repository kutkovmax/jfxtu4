package ru.kutkovmax.jfxtu4.controller;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.kutkovmax.jfxtu4.model.ParsingException;
import ru.kutkovmax.jfxtu4.model.Program;
import ru.kutkovmax.jfxtu4.model.ProgramParser;
import ru.kutkovmax.jfxtu4.model.TuringMachine;
import ru.kutkovmax.jfxtu4.view.TapeView;

public class MachineController {

    private final TapeView tapeView;
    private final TextArea programInput;
    private final Button startButton;
    private final Button backToEditButton;
    private final Button quickButton;
    private final Button stepButton;

    private TuringMachine machine;

    public MachineController(TapeView tapeView, TextArea programInput,
                             Button startButton, Button backToEditButton,
                             Button quickButton, Button stepButton) {
        this.tapeView = tapeView;
        this.programInput = programInput;
        this.startButton = startButton;
        this.backToEditButton = backToEditButton;
        this.quickButton = quickButton;
        this.stepButton = stepButton;

        initHandlers();
    }

    private void initHandlers() {
        startButton.setOnAction(event -> handleStart());
        stepButton.setOnAction(event -> handleStep());
        quickButton.setOnAction(event -> handleQuickRun());
        backToEditButton.setOnAction(event -> handleBackToEdit());
    }

    private void handleStart() {
        String programText = programInput.getText();
        try {
            Program program = ProgramParser.parse(programText);
            tapeView.getTape().moveHeadToEndOfInput();
            this.machine = new TuringMachine(tapeView.getTape(), program);
            startButton.setVisible(false);
            stepButton.setVisible(true);
            quickButton.setVisible(true);
            backToEditButton.setVisible(true);
        } catch (ParsingException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
}

    private void handleStep() {
        machine.step();
        tapeView.renderTape();
        if (machine.isStalled()){
            handleBackToEdit();
        }
    }

    private void handleQuickRun() {
        while (!machine.isStalled()) {
            machine.step();
            tapeView.renderTape();
        }
        handleBackToEdit();
    }

    private void handleBackToEdit() {
        startButton.setVisible(true);
        stepButton.setVisible(false);
        quickButton.setVisible(false);
        backToEditButton.setVisible(false);
    }
}