package ru.kutkovmax.jfxtu4.model;

import java.util.ArrayList;
import java.util.List;

public class Tape {
    private int head;
    private final List<Character> cells;
    private String inputString;

    private static final char BLANK_CHAR = ' ';
    private final Object lock = new Object();

    public Tape(String initialInput) {
        this.cells = new ArrayList<>();

        this.inputString = (initialInput == null) ? "" : initialInput;

        if (initialInput == null || initialInput.isEmpty()) {
            this.cells.add(BLANK_CHAR);
            this.head = 0;
        } else {
            for (char c : initialInput.toCharArray()) {
                this.cells.add(c);
            }
            this.head = initialInput.length();
            ensureCapacity();
        }
    }

    public char read(){
        synchronized (lock) {
            return cells.get(head);
        }
    }

    public void write(char character){
        synchronized (lock) {
            cells.set(head, character);
        }
    }

    public void moveLeft(){
        synchronized (lock) {
            if (head == 0) {
                throw new MachineException(MachineException.Type.HEAD_OUT_OF_BOUNDS);
            }
            head--;
        }
    }

    public void moveRight(){
        synchronized (lock) {
            head++;
            ensureCapacity();
        }
    }

    private void ensureCapacity() {
        while (head >= cells.size()) {
            cells.add(BLANK_CHAR);
        }
    }

    public void moveHeadToEndOfInput() {
        synchronized (lock) {
            int lastNonBlank = -1;

            for (int i = 0; i < cells.size(); i++) {
                if (cells.get(i) != BLANK_CHAR) {
                    lastNonBlank = i;
                }
            }

            if (lastNonBlank == -1) {
                inputString = "";
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= lastNonBlank; i++) {
                    sb.append(cells.get(i));
                }
                inputString = sb.toString();
            }

            head = lastNonBlank + 1;
            ensureCapacity();
        }
    }

    public void restoreInput() {
        synchronized (lock) {
            cells.clear();
            if (inputString.isEmpty()) {
                cells.add(BLANK_CHAR);
                head = 0;
            } else {
                for (char c : inputString.toCharArray()) {
                    cells.add(c);
                }
                head = inputString.length();
            }
        }
    }

    public boolean isInputPreserved() {
        synchronized (lock) {
            if (inputString == null) {
                return true;
            }

            for (int i = 0; i < inputString.length(); i++) {
                if (cells.get(i) != inputString.charAt(i)) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean isHeadAfterResult() {
        synchronized (lock) {
            int lastNonBlank = -1;

            for (int i = cells.size() - 1; i >= 0; i--) {
                if (cells.get(i) != BLANK_CHAR) {
                    lastNonBlank = i;
                    break;
                }
            }

            return head == lastNonBlank + 1;
        }
    }

    // -- ui accessors --

    public int getHeadPosition() {
        synchronized (lock) {
            return head;
        }
    }

    public void setHeadPosition(int position) {
        synchronized (lock) {
            if (position < 0) return;
            this.head = position;
            ensureCapacity();
        }
    }

    public int getCellsSize() {
        synchronized (lock) {
            return cells.size();
        }
    }

    public char readAt(int index) {
        synchronized (lock) {
            if (index < 0 || index >= cells.size()) {
                return BLANK_CHAR;
            }
            Character c = cells.get(index);
            return c == null ? BLANK_CHAR : c;
        }
    }

    public void writeAt(int index, char character) {
        synchronized (lock) {
            if (index < 0) return;
            while (index >= cells.size()) {
                cells.add(BLANK_CHAR);
            }
            cells.set(index, character);
        }
    }

    // -- snapshot for UI rendering --

    public TapeSnapshotForRender snapshotForRender() {
        synchronized (lock) {
            char[] copy = new char[cells.size()];
            for (int i = 0; i < cells.size(); i++) {
                Character c = cells.get(i);
                copy[i] = (c == null) ? BLANK_CHAR : c;
            }
            return new TapeSnapshotForRender(copy, head);
        }
    }

    public record TapeSnapshotForRender(char[] cellsCopy, int headPosition) {}
}
