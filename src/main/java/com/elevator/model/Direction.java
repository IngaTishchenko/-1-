package com.elevator.model;

/**
 * Направление движения лифта или заявки.
 */
public enum Direction {
    /** Вверх */
    UP,
    /** Вниз */
    DOWN;

    /**
     * Возвращает противоположное направление.
     *
     * @return противоположное направление
     */
    public Direction opposite() {
        return this == UP ? DOWN : UP;
    }
}
