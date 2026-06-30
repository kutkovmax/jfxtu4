package ru.kutkovmax.jfxtu4.model;

public class ParsingException extends RuntimeException {

    public enum ErrorType {
        EMPTY_CODE,
        INVALID_FORMAT,
        MISSING_STATE_NAME,
        TOO_LONG_SYMBOL,
        DUPLICATE_TRANSITION,
        NO_START_STATE
    }

    private final ErrorType type;
    private final int lineNumber;
    private final String token;

    public ParsingException(ErrorType type) {
        super(type.name());
        this.type = type;
        this.lineNumber = -1;
        this.token = null;
    }

    public ParsingException(ErrorType type, int lineNumber, String token) {
        super(type.name() + " at line " + lineNumber);
        this.type = type;
        this.lineNumber = lineNumber;
        this.token = token;
    }

    public ErrorType getType() { return type; }
    public int getLineNumber() { return lineNumber; }
    public String getToken() { return token; }
}