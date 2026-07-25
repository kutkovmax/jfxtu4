package ru.kutkovmax.jfxtu4.model;

public class TuringMachine {
    private final Tape tape;
    private final Program program;
    private int currentStateId;
    private boolean stalled = false;

    public TuringMachine(Tape tape, Program program) {
        this.tape = tape;
        this.program = program;
        this.currentStateId = program.startState();
    }

    public void step() {
        if (stalled) return;

        char currentSymbol = tape.read();
        TransitionKey key = new TransitionKey(currentStateId, currentSymbol);
        TransitionResult instruction = program.transitionTable().get(key);

        if (instruction == null) {
            stalled = true;
            throw new MachineException(
                    MachineException.Type.UNDEFINED_TRANSITION,
                    currentStateId,
                    String.valueOf(currentSymbol)
            );
        }

        char action = instruction.symbolToWrite();
        if (action == '>') {
            tape.moveRight();
        } else if (action == '<') {
            tape.moveLeft();
        } else if (action == '#') {
            stalled = true;
        } else if (action == '='){

        } else {
            tape.write(action);
        }

        currentStateId = instruction.nextState();
    }

    public boolean isStalled() {
        return stalled;
    }

    public int getNextLineIndex() {
        if (stalled) return -1;

        char currentSymbol = tape.read();
        TransitionKey key = new TransitionKey(currentStateId, currentSymbol);
        TransitionResult instruction = program.transitionTable().get(key);

        return instruction != null ? instruction.lineIndex() : -1;
    }
}