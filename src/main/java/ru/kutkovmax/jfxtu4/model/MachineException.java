package ru.kutkovmax.jfxtu4.model;

public class MachineException extends RuntimeException {
    public enum Type {
        HEAD_OUT_OF_BOUNDS("error.head_out_of_bounds"),
        UNDEFINED_TRANSITION("error.undefined_transition");

        private final String key;

        Type(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    private final Type type;
    private final Object[] args;

    public MachineException(Type type, Object... args) {
        this.type = type;
        this.args = args;
    }

    public Type getType() { return type; }
    public Object[] getArgs() { return args; }
}