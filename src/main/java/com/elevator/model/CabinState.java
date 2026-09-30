package com.elevator.model;

/**
 * Состояние кабины лифта (машина состояний).
 * <p>
 * Переходы:
 * <ul>
 *   <li>{@link #IDLE} → {@link #MOVING} при появлении цели</li>
 *   <li>{@link #MOVING} → {@link #OPENING_DOORS} при достижении этажа</li>
 *   <li>{@link #OPENING_DOORS} → {@link #WAITING} после открытия</li>
 *   <li>{@link #WAITING} → {@link #MOVING} или {@link #IDLE} после посадки</li>
 * </ul>
 */
public enum CabinState {
    /** Стоит, нет активных целей */
    IDLE("Стоит"),
    /** Движется к цели */
    MOVING("Едет"),
    /** Открывает двери */
    OPENING_DOORS("Открывает двери"),
    /** Ждёт посадки/высадки пассажиров */
    WAITING("Ждёт посадки");

    private final String description;

    CabinState(String description) {
        this.description = description;
    }

    /**
     * Человекочитаемое описание состояния.
     *
     * @return описание
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
