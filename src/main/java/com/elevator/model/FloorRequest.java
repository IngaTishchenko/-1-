package com.elevator.model;

import java.util.Objects;

/**
 * Заявка на вызов лифта: этаж, направление и момент нажатия (модельное время).
 * <p>
 * Неизменяемый value-object. equals/hashCode по всем полям.
 *
 * @param floor     номер этажа
 * @param direction желаемое направление
 * @param time      момент нажатия кнопки (модельные тики)
 */
public record FloorRequest(int floor, Direction direction, long time) {

    /**
     * Создаёт заявку с проверкой корректности направления.
     *
     * @param floor     этаж
     * @param direction направление
     * @param time      время
     * @throws NullPointerException если direction == null
     */
    public FloorRequest {
        Objects.requireNonNull(direction, "direction must not be null");
        if (time < 0) {
            throw new IllegalArgumentException("time must be non-negative: " + time);
        }
    }

    /**
     * Создаёт заявку без явного времени (время = 0).
     *
     * @param floor     этаж
     * @param direction направление
     * @return новая заявка
     */
    public static FloorRequest of(int floor, Direction direction) {
        return new FloorRequest(floor, direction, 0L);
    }
}
