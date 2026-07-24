package ru.kutkovmax.jfxtu4.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProgramParserTest {

    @Test
    void testSuccessfulParsing() {
        String validCode = "0, ,<,rep\nrep,0,1,mov";
        Program program = ProgramParser.parse(validCode);

        assertNotNull(program);
        int startId = program.stateTable().getId("0");
        assertEquals(startId, program.startState());
    }

    @Test
    void testSpacesAsSymbolsAndActions() {
        String codeWithSpaces = "0, , ,next";
        Program program = ProgramParser.parse(codeWithSpaces);

        int state0Id = program.stateTable().getId("0");
        TransitionKey key = new TransitionKey(state0Id, ' ');

        assertTrue(program.transitionTable().containsKey(key));

        TransitionResult result = program.transitionTable().get(key);
        assertEquals(' ', result.symbolToWrite());
        assertEquals(program.stateTable().getId("next"), result.nextState());
    }

    @Test
    void testNormalizeStates() {
        String code = "00,0,1,01\n1,1,0,2";
        Program program = ProgramParser.parse(code);

        int state0Id = program.stateTable().getId("0");
        int state1Id = program.stateTable().getId("1");

        TransitionKey key0 = new TransitionKey(state0Id, '0');
        assertTrue(program.transitionTable().containsKey(key0));
        assertEquals(state1Id, program.transitionTable().get(key0).nextState());
    }

    @Test
    void testMultipleCommandsInOneLine() {
        String singleLine = "0,0,1,1      1,1,0,2    2, ,#,stop";
        Program program = ProgramParser.parse(singleLine);

        int s0 = program.stateTable().getId("0");
        int s1 = program.stateTable().getId("1");
        int s2 = program.stateTable().getId("2");

        assertTrue(program.transitionTable().containsKey(new TransitionKey(s0, '0')));
        assertTrue(program.transitionTable().containsKey(new TransitionKey(s1, '1')));
        assertTrue(program.transitionTable().containsKey(new TransitionKey(s2, ' ')));
    }

    @Test
    void testCommentsHandlingAndTrickySymbols() {
        String codeWithComments =
                "# Полный коммент в начале строки\n" +
                        "0,#,>,rep // Коммент после команды через пробел\n" +
                        "rep,0,1,stop   # Еще один коммент через пробел";

        Program program = ProgramParser.parse(codeWithComments);
        int s0 = program.stateTable().getId("0");

        // Проверяем, что решетка без пробела распарсилась как символ ленты
        assertTrue(program.transitionTable().containsKey(new TransitionKey(s0, '#')));
    }

    @Test
    void testWindowsAndTrailingLines() {
        String windowsCode = "0,0,1,stop\r\n\r\n   \r\n";
        Program program = ProgramParser.parse(windowsCode);
        assertNotNull(program);
    }

    @Test
    void testEmptyCodeThrowsException() {
        ParsingException exNull = assertThrows(ParsingException.class, () -> ProgramParser.parse(null));
        assertEquals(ParsingException.ErrorType.EMPTY_CODE, exNull.getType());

        ParsingException exBlank = assertThrows(ParsingException.class, () -> ProgramParser.parse("   \n   "));
        assertEquals(ParsingException.ErrorType.EMPTY_CODE, exBlank.getType());
    }

    @Test
    void testNoStartStateThrowsException() {
        String noStartCode = "start,0,1,stop";
        ParsingException exception = assertThrows(ParsingException.class, () -> ProgramParser.parse(noStartCode));
        assertEquals(ParsingException.ErrorType.NO_START_STATE, exception.getType());
    }

    @Test
    void testInvalidFormatThrowsException() {
        ParsingException exception = assertThrows(ParsingException.class, () -> ProgramParser.parse("0, ,rep"));
        assertEquals(ParsingException.ErrorType.INVALID_FORMAT, exception.getType());
        assertEquals(1, exception.getLineNumber());
        assertArrayEquals(new Object[]{"0, ,rep"}, exception.getArgs());

        ParsingException exception2 = assertThrows(ParsingException.class, () -> ProgramParser.parse("0,0,1,2,stop"));
        assertEquals(ParsingException.ErrorType.INVALID_FORMAT, exception2.getType());
        assertArrayEquals(new Object[]{"0,0,1,2,stop"}, exception2.getArgs());
    }

    @Test
    void testMissingStateNameThrowsException() {
        ParsingException exLeft = assertThrows(ParsingException.class, () -> ProgramParser.parse(",0,1,next"));
        assertEquals(ParsingException.ErrorType.MISSING_STATE_NAME, exLeft.getType());

        ParsingException exRight = assertThrows(ParsingException.class, () -> ProgramParser.parse("0,0,1,"));
        assertEquals(ParsingException.ErrorType.MISSING_STATE_NAME, exRight.getType());
    }

    @Test
    void testTooLongSymbolThrowsException() {
        ParsingException exSym = assertThrows(ParsingException.class, () -> ProgramParser.parse("0,00,1,next"));
        assertEquals(ParsingException.ErrorType.TOO_LONG_SYMBOL, exSym.getType());

        ParsingException exAct = assertThrows(ParsingException.class, () -> ProgramParser.parse("0,0,==,next"));
        assertEquals(ParsingException.ErrorType.TOO_LONG_SYMBOL, exAct.getType());
    }

    @Test
    void testDuplicateTransitionThrowsException() {
        String duplicateCode = "0,0,1,next\n0,0,>,error";
        ParsingException exception = assertThrows(ParsingException.class, () -> ProgramParser.parse(duplicateCode));
        assertEquals(ParsingException.ErrorType.DUPLICATE_TRANSITION, exception.getType());
        assertEquals(2, exception.getLineNumber());
        assertArrayEquals(new Object[]{"0", '0'}, exception.getArgs());
    }
}