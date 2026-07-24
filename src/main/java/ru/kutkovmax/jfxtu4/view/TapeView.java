package ru.kutkovmax.jfxtu4.view;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import ru.kutkovmax.jfxtu4.model.Tape;

public class TapeView extends HBox {
    private final Tape tape;
    private int startIndex = 0;
    private static final int SPACING = 2;
    private static final double CELL_SIZE = 40;
    private static final double CELL_WIDTH = CELL_SIZE + SPACING;
    private int lastVisibleCells = 0;
    private boolean editable = true;

    public TapeView(Tape tape) {
        this.tape = tape;
        this.setSpacing(SPACING);
        widthProperty().addListener((obs, oldWidth, newWidth) -> {

            int newVisibleCells = getVisibleCells();

            if (newVisibleCells != lastVisibleCells) {
                lastVisibleCells = newVisibleCells;
                renderTape();
            }
        });
    }

    public void renderTape() {
        this.getChildren().clear();

        int cells = getVisibleCells();
        if (cells <= 0) {
            return;
        }

        for (int i = 0; i < cells; i++) {
            final int visualIndex = i;
            final int absoluteIndex = startIndex + visualIndex;

            TextField cell = new TextField(String.valueOf(tape.readAt(absoluteIndex)));
            cell.setPrefSize(CELL_SIZE, CELL_SIZE);
            cell.getStyleClass().add("tape-cell");
            cell.setEditable(this.editable);

            if (!editable && absoluteIndex == tape.getHeadPosition()) {
                cell.getStyleClass().add("head-active");
            }

            cell.setOnKeyPressed(event -> {
                if (!editable) return;
                if (event.getCode() == KeyCode.RIGHT) {
                    handleMoveRight(visualIndex);
                } else if (event.getCode() == KeyCode.LEFT) {
                    handleMoveLeft(visualIndex);
                } else if (event.getCode() == KeyCode.BACK_SPACE) {
                    tape.writeAt(absoluteIndex, ' ');
                    cell.setText(" ");
                    handleMoveLeft(visualIndex);
                }
            });

            cell.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!editable) return;
                if (!newVal.isEmpty()) {
                    char c = newVal.charAt(newVal.length() - 1);
                    tape.writeAt(absoluteIndex, c);
                    cell.setText(String.valueOf(c));
                    handleMoveRight(visualIndex);
                }
            });

            this.getChildren().add(cell);
        }
    }

    public Tape getTape(){
        return tape;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        renderTape();
    }


    private void handleMoveRight(int currentVisualIndex) {
        if (currentVisualIndex == getVisibleCells() - 1) {
            startIndex++;
            renderTape();
            focusVisualCell(getVisibleCells() - 1);
        } else {
            focusVisualCell(currentVisualIndex + 1);
        }
    }

    private void handleMoveLeft(int currentVisualIndex) {
        if (currentVisualIndex == 0) {
            if (startIndex > 0) {
                startIndex--;
                renderTape();
                focusVisualCell(0);
            }
        }else{
            focusVisualCell(currentVisualIndex - 1);
        }
    }

    private void focusVisualCell(int visualIndex) {
        if (visualIndex >= 0 && visualIndex < this.getChildren().size()) {
            this.getChildren().get(visualIndex).requestFocus();
        }
    }

    private int getVisibleCells() {
        return (int) (getWidth() / CELL_WIDTH);
    }

}