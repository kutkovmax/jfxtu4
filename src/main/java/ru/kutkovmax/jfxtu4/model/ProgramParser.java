package ru.kutkovmax.jfxtu4.model;

import java.util.HashMap;
import java.util.Map;

public class ProgramParser {

    private static final String START_STATE_NAME = "0";

    public static Program parse(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new ParsingException(ParsingException.ErrorType.EMPTY_CODE);
        }

        Map<TransitionKey, TransitionResult> transitionTable = new HashMap<>();
        StateTable stateTable = new StateTable();

        String[] lines = rawCode.split("\n");
        boolean hasStartState = false;

        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];
            int lineNumber = lineIdx + 1;

            String cleanLine = removeComments(line).trim();
            if (cleanLine.isBlank()) {
                continue;
            }

            int currentPos = 0;
            while (currentPos < cleanLine.length()) {
                while (currentPos < cleanLine.length() && Character.isWhitespace(cleanLine.charAt(currentPos))) {
                    currentPos++;
                }
                if (currentPos >= cleanLine.length()) break;

                int commaCount = 0;
                int endPos = currentPos;

                while (endPos < cleanLine.length()) {
                    char ch = cleanLine.charAt(endPos);
                    if (ch == ',') {
                        commaCount++;
                    }
                    if (commaCount == 3 && Character.isWhitespace(ch)) {
                        break;
                    }
                    endPos++;
                }

                String token = cleanLine.substring(currentPos, endPos).trim();
                currentPos = endPos;

                if (token.isEmpty()) continue;

                String[] parts = token.split(",", -1);

                if (parts.length != 4) {
                    throw new ParsingException(ParsingException.ErrorType.INVALID_FORMAT, lineNumber, token);
                }

                String qStr = normalizeState(parts[0]);
                String aStr = parts[1];
                String vStr = parts[2].trim();
                String qNextStr = normalizeState(parts[3]);

                if (qStr.isEmpty() || qNextStr.isEmpty()) {
                    throw new ParsingException(ParsingException.ErrorType.MISSING_STATE_NAME, lineNumber, qStr.isEmpty() ? qNextStr : qStr);
                }

                if (aStr.length() > 1 || vStr.length() > 1) {
                    throw new ParsingException(ParsingException.ErrorType.TOO_LONG_SYMBOL, lineNumber, token);
                }

                if (qStr.equals(START_STATE_NAME)) {
                    hasStartState = true;
                }

                int qId = stateTable.getId(qStr);
                int qNextId = stateTable.getId(qNextStr);

                char symbol = aStr.isEmpty() ? ' ' : aStr.charAt(0);
                char action = vStr.isEmpty() ? ' ' : vStr.charAt(0);

                TransitionKey key = new TransitionKey(qId, symbol);
                if (transitionTable.containsKey(key)) {
                    throw new ParsingException(ParsingException.ErrorType.DUPLICATE_TRANSITION, lineNumber, qStr, symbol);
                }

                transitionTable.put(key, new TransitionResult(qNextId, action, lineIdx));
            }
        }

        if (!hasStartState) {
            throw new ParsingException(ParsingException.ErrorType.NO_START_STATE);
        }

        int startStateId = stateTable.getId(START_STATE_NAME);

        return new Program(transitionTable, stateTable, startStateId);
    }

    private static String removeComments(String line) {
        if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
            return "";
        }
        int commentIdx = line.indexOf(" //");
        if (commentIdx == -1) {
            commentIdx = line.indexOf(" #");
        }
        return (commentIdx != -1) ? line.substring(0, commentIdx) : line;
    }

    private static String normalizeState(String state) {
        String trimmed = state.trim();
        try {
            return String.valueOf(Integer.parseInt(trimmed));
        } catch (NumberFormatException e) {
            return trimmed;
        }
    }
}