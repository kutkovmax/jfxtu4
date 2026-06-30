package ru.kutkovmax.jfxtu4.model;

import java.util.Map;

public record Program(
        Map<TransitionKey, TransitionResult> transitionTable,
        StateTable stateTable,
        int startState
) {}
