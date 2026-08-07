package ru.kutkovmax.jfxtu4.view;

import javafx.animation.AnimationTimer;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ru.kutkovmax.jfxtu4.model.Tape;

public class TapeView extends Canvas {
    private final Tape tape;

    private static final double CELL_SIZE = 40;
    private static final double FONT_SIZE = 20;
    private static final String FONT_FAMILY = "JetBrains Mono";

    private static final Color CANVAS_BG_COLOR = Color.web("#2b2b2b");
    private static final Color CELL_BG_COLOR = Color.web("#1e1e1e");
    private static final Color BORDER_COLOR = Color.web("#555555");
    private static final Color TEXT_COLOR = Color.web("#ffffff");

    private static final Color HEAD_BG_COLOR = Color.web("#284B72");
    private static final Color HEAD_BORDER_COLOR = Color.web("#ffffff");

    private static final Color SELECTION_COLOR = Color.web("#4a75a3", 0.6);

    private boolean editable = true;

    private int targetStartIndex = 0;
    private double currentVisualPixelOffset = 0;

    private int cursorIndex = 0;
    private int selectionAnchor = -1;

    private boolean needsRedraw = true;

    private Runnable onRunAction;
    private Runnable onToggleFastModeAction;
    private Runnable onStepAction;
    private Runnable onStopAction;

    public TapeView(Tape tape) {
        this.tape = tape;
        setFocusTraversable(true);

        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseClicked(e -> {
            requestFocus();
            needsRedraw = true;
        });

        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        addEventFilter(KeyEvent.KEY_TYPED, this::handleKeyTyped);

        widthProperty().addListener(e -> {
            ensureCursorVisible();
            needsRedraw = true;
        });

        focusedProperty().addListener((obs, oldVal, isFocused) -> {
            if (!isFocused) {
                selectionAnchor = -1;
            }
            needsRedraw = true;
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateAnimation();
                if (needsRedraw) {
                    render();
                    needsRedraw = false;
                }
            }
        };
        timer.start();
    }

    public void setOnRunAction(Runnable onRunAction) {
        this.onRunAction = onRunAction;
    }

    public void setOnToggleFastModeAction(Runnable onToggleFastModeAction) {
        this.onToggleFastModeAction = onToggleFastModeAction;
    }

    public void setOnStepAction(Runnable onStepAction) {
        this.onStepAction = onStepAction;
    }

    public void setOnStopAction(Runnable onStopAction) {
        this.onStopAction = onStopAction;
    }

    public void renderTape() {
        if (!editable) {
            ensureVisible(tape.getHeadPosition());
        } else {
            ensureCursorVisible();
        }
        needsRedraw = true;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        this.selectionAnchor = -1;
        if (!editable) {
            ensureVisible(tape.getHeadPosition());
        }
        needsRedraw = true;
    }

    public Tape getTape() {
        return tape;
    }

    private void updateAnimation() {
        double targetPixelOffset = targetStartIndex * CELL_SIZE;

        if (Math.abs(targetPixelOffset - currentVisualPixelOffset) > 0.5) {
            currentVisualPixelOffset += (targetPixelOffset - currentVisualPixelOffset) * 0.15;
            needsRedraw = true;
        } else if (currentVisualPixelOffset != targetPixelOffset) {
            currentVisualPixelOffset = targetPixelOffset;
            needsRedraw = true;
        }
    }

    private void render() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(CANVAS_BG_COLOR);
        gc.fillRect(0, 0, w, h);

        if (w == 0 || h == 0) return;

        Tape.TapeSnapshotForRender snap = tape.snapshotForRender();
        char[] cells = snap.cellsCopy();
        int headPos = snap.headPosition();

        gc.setFont(Font.font(FONT_FAMILY, FONT_SIZE));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        int visibleCellsCount = (int) (w / CELL_SIZE) + 2;
        int firstVisibleIndex = Math.max(0, (int) (currentVisualPixelOffset / CELL_SIZE));

        double y = (h - CELL_SIZE) / 2;

        for (int i = 0; i < visibleCellsCount; i++) {
            int index = firstVisibleIndex + i;
            double x = (index * CELL_SIZE) - currentVisualPixelOffset;

            gc.setFill(CELL_BG_COLOR);
            gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);

            if (isIndexSelected(index)) {
                gc.setFill(SELECTION_COLOR);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            }

            gc.setStroke(BORDER_COLOR);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);

            if (editable && index == cursorIndex && isFocused()) {
                gc.setStroke(HEAD_BORDER_COLOR);
                gc.setLineWidth(2);
                gc.strokeRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            }

            char c = (index < 0 || index >= cells.length) ? BLANK_CHAR_FALLBACK : cells[index];
            if (c != BLANK_CHAR_FALLBACK) {
                gc.setFill(TEXT_COLOR);
                gc.fillText(String.valueOf(c), x + CELL_SIZE / 2, y + CELL_SIZE / 2);
            }
        }

        if (!editable) {
            double headX = (headPos * CELL_SIZE) - currentVisualPixelOffset;

            gc.setFill(HEAD_BG_COLOR);
            gc.fillRect(headX, y, CELL_SIZE, CELL_SIZE);

            gc.setStroke(HEAD_BORDER_COLOR);
            gc.setLineWidth(2);
            gc.strokeRect(headX, y, CELL_SIZE, CELL_SIZE);

            char c = (headPos < 0 || headPos >= cells.length) ? BLANK_CHAR_FALLBACK : cells[headPos];
            if (c != BLANK_CHAR_FALLBACK) {
                gc.setFill(TEXT_COLOR);
                gc.fillText(String.valueOf(c), headX + CELL_SIZE / 2, y + CELL_SIZE / 2);
            }
        }
    }

    private static final char BLANK_CHAR_FALLBACK = ' ';

    private void ensureCursorVisible() {
        ensureVisible(cursorIndex);
    }

    public void ensureVisible(int index) {
        int visibleCellsCount = (int) (getWidth() / CELL_SIZE);

        if (index < targetStartIndex) {
            targetStartIndex = Math.max(0, index);
            needsRedraw = true;
        } else if (index >= targetStartIndex + visibleCellsCount) {
            targetStartIndex = index - visibleCellsCount + 1;
            needsRedraw = true;
        }
    }

    private void handleMousePressed(MouseEvent event) {
        requestFocus();
        event.consume();

        double y = (getHeight() - CELL_SIZE) / 2;
        if (event.getY() < y || event.getY() > y + CELL_SIZE) {
            selectionAnchor = -1;
            needsRedraw = true;
            return;
        }

        int clickedIndex = getIndexFromMouseX(event.getX());
        cursorIndex = clickedIndex;
        selectionAnchor = -1;
        ensureCursorVisible();
        needsRedraw = true;
    }

    private void handleMouseDragged(MouseEvent event) {
        event.consume();

        if (selectionAnchor == -1) {
            selectionAnchor = cursorIndex;
        }
        cursorIndex = getIndexFromMouseX(event.getX());
        ensureCursorVisible();
        needsRedraw = true;
    }

    private int getIndexFromMouseX(double mouseX) {
        int index = (int) ((mouseX + currentVisualPixelOffset) / CELL_SIZE);
        return Math.max(0, index);
    }

    private boolean isIndexSelected(int index) {
        if (selectionAnchor == -1) return false;

        int min = Math.min(cursorIndex, selectionAnchor);
        int max = Math.max(cursorIndex, selectionAnchor);
        return index >= min && index <= max;
    }

    private void handleKeyPressed(KeyEvent event) {
        if (!editable) {
            if (event.getCode() == KeyCode.ENTER) {
                if (onToggleFastModeAction != null) onToggleFastModeAction.run();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.SPACE) {
                if (onStepAction != null) onStepAction.run();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.ESCAPE) {
                if (onStopAction != null) onStopAction.run();
                event.consume();
                return;
            }
        }

        if (editable && event.getCode() == KeyCode.ENTER) {
            if (onRunAction != null) onRunAction.run();
            event.consume();
            return;
        }

        if (editable && (event.isControlDown() || event.isShortcutDown()) && event.getCode() == KeyCode.BACK_SPACE) {
            deleteWordBackward();
            event.consume();
            return;
        }

        if (event.isControlDown() || event.isShortcutDown()) {
            if (event.getCode() == KeyCode.C) {
                copySelection();
                event.consume();
            } else if (event.getCode() == KeyCode.V && editable) {
                pasteFromClipboard();
                event.consume();
            }
            return;
        }

        switch (event.getCode()) {
            case LEFT -> {
                cursorIndex = Math.max(0, cursorIndex - 1);
                selectionAnchor = -1;
                ensureCursorVisible();
                needsRedraw = true;
                event.consume();
            }
            case RIGHT -> {
                cursorIndex++;
                selectionAnchor = -1;
                ensureCursorVisible();
                needsRedraw = true;
                event.consume();
            }
            case BACK_SPACE -> {
                if (!editable) return;
                if (selectionAnchor != -1) {
                    deleteSelection();
                } else {
                    tape.writeAt(cursorIndex, ' ');
                    cursorIndex = Math.max(0, cursorIndex - 1);
                    ensureCursorVisible();
                }
                needsRedraw = true;
                event.consume();
            }
            case DELETE -> {
                if (!editable) return;
                if (selectionAnchor != -1) {
                    deleteSelection();
                } else {
                    tape.writeAt(cursorIndex, ' ');
                    cursorIndex = cursorIndex + 1;
                    ensureCursorVisible();
                }
                needsRedraw = true;
                event.consume();
            }
        }
    }

    private void handleKeyTyped(KeyEvent event) {
        if (!editable || event.isControlDown() || event.isShortcutDown()) return;

        String character = event.getCharacter();
        if (character.length() > 0) {
            char c = character.charAt(0);

            if (!Character.isISOControl(c)) {
                if (selectionAnchor != -1) {
                    deleteSelection();
                }
                tape.writeAt(cursorIndex, c);
                cursorIndex++;
                ensureCursorVisible();
                needsRedraw = true;
                event.consume();
            }
        }
    }

    private void deleteWordBackward() {
        if (selectionAnchor != -1) {
            deleteSelection();
            return;
        }

        if (cursorIndex <= 0) return;

        int i = cursorIndex - 1;

        while (i >= 0 && (tape.readAt(i) == ' ' || tape.readAt(i) == '\0')) {
            i--;
        }

        while (i >= 0 && tape.readAt(i) != ' ' && tape.readAt(i) != '\0') {
            tape.writeAt(i, ' ');
            i--;
        }

        cursorIndex = Math.max(0, i + 1);
        ensureCursorVisible();
        needsRedraw = true;
    }

    private void copySelection() {
        int start = selectionAnchor == -1 ? cursorIndex : Math.min(cursorIndex, selectionAnchor);
        int end = selectionAnchor == -1 ? cursorIndex : Math.max(cursorIndex, selectionAnchor);

        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= end; i++) {
            char c = tape.readAt(i);
            sb.append(c == '\0' ? ' ' : c);
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void pasteFromClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            String text = clipboard.getString();

            if (selectionAnchor != -1) deleteSelection();

            int limit = Math.min(text.length(), 10_000);
            for (int i = 0; i < limit; i++) {
                char c = text.charAt(i);
                if (!Character.isISOControl(c)) {
                    tape.writeAt(cursorIndex + i, c);
                }
            }
            cursorIndex += limit;
            ensureCursorVisible();
            needsRedraw = true;
        }
    }

    private void deleteSelection() {
        int start = Math.min(cursorIndex, selectionAnchor);
        int end = Math.max(cursorIndex, selectionAnchor);
        for (int i = start; i <= end; i++) {
            tape.writeAt(i, ' ');
        }
        cursorIndex = start;
        selectionAnchor = -1;
        ensureCursorVisible();
        needsRedraw = true;
    }
}