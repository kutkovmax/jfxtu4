package ru.kutkovmax.jfxtu4.model;

import java.util.ArrayList;
import java.util.List;

public class Tape {
    private int head;
    private final List<Character> cells;
    private static final char BLANK_CHAR = ' ';

    public Tape(String initialInput) {
        this.cells = new ArrayList<>();

        if (initialInput == null || initialInput.isEmpty()) {
            this.cells.add(BLANK_CHAR);
            this.head = 0;
        } else {
            for (char c : initialInput.toCharArray()) {
                this.cells.add(c);
            }
            this.head = initialInput.length();
            ensureCapacity(); // spawns next blank cell right of input data
        }
    }

    public char read(){
        return cells.get(head);
    }

    public void write(char character){
        cells.set(head, character);
    }

    public void moveLeft(){
        if (head == 0) {
            throw new IllegalStateException("Tape error: Left border reached.");
        }
        head--;
    }

    public void moveRight(){
        head++;
        ensureCapacity();
    }

    private void ensureCapacity() {
        while (head >= cells.size()) {
            cells.add(BLANK_CHAR);
        }
    }

    public void moveHeadToEndOfInput() {
        System.out.println("MOVE HEAD");
        int lastNonBlank = -1;

        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i) != BLANK_CHAR) {
                lastNonBlank = i;
            }
        }

        head = lastNonBlank + 1;
        ensureCapacity();
        System.out.println("head = " + head);
    }

    // -- ui accessors --

    public int getHeadPosition() {
        return head;
    }

    public void setHeadPosition(int position) {
        if (position < 0) return;
        this.head = position;
        ensureCapacity();
    }

    public int getCellsSize() {
        return cells.size();
    }

    public char readAt(int index) {
        if (index < 0 || index >= cells.size()) {
            return BLANK_CHAR;
        }
        return cells.get(index);
    }

    public void writeAt(int index, char character) {
        if (index < 0) return;
        while (index >= cells.size()) {
            cells.add(BLANK_CHAR);
        }
        cells.set(index, character);
    }

}
