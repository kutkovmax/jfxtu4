package ru.kutkovmax.jfxtu4.model;

public class ParsingException extends RuntimeException {

    public enum ErrorType {
        EMPTY_CODE("error.program_is_empty"),
        INVALID_FORMAT("error.parse"),
        MISSING_STATE_NAME("error.target_state_missing"),
        TOO_LONG_SYMBOL("error.parse"),
        DUPLICATE_TRANSITION("error.ambiguous_command"),
        NO_START_STATE("error.initial_state_missing");

        private final String key;

        ErrorType(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    private final ErrorType type;
    private final int lineNumber;
    private final Object[] args;

    public ParsingException(ErrorType type) {
        super(type.name());
        this.type = type;
        this.lineNumber = -1;
        this.args = new Object[0];
    }

    public ParsingException(ErrorType type, int lineNumber, Object... args) {
        super(type.name() + " at line " + lineNumber);
        this.type = type;
        this.lineNumber = lineNumber;
        this.args = args;
    }

    public ErrorType getType() { return type; }
    public int getLineNumber() { return lineNumber; }
    public Object[] getArgs() { return args; }
}