package com.elevator.model;

import java.util.Objects;

/**
 * Кабина лифта: текущий этаж, состояние, направление.
 * <p>
 * Инкапсулирует изменяемое состояние. Идентификация по id.
 */
public final class Elevator {

    private final String id;
    private int currentFloor;
    private CabinState state;
    private Direction direction; // null, если IDLE

    /**
     * Создаёт кабину на указанном этаже в состоянии IDLE.
     *
     * @param id           идентификатор кабины
     * @param initialFloor начальный этаж
     * @throws NullPointerException     если id == null
     * @throws IllegalArgumentException если id пустой
     */
    public Elevator(String id, int initialFloor) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.id = id;
        this.currentFloor = initialFloor;
        this.state = CabinState.IDLE;
        this.direction = null;
    }

    /**
     * @return идентификатор
     */
    public String getId() {
        return id;
    }

    /**
     * @return текущий этаж
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    /**
     * @return текущее состояние
     */
    public CabinState getState() {
        return state;
    }

    /**
     * @return текущее направление или {@code null}, если стоит
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Переводит кабину в указанное состояние.
     *
     * @param newState новое состояние
     */
    public void setState(CabinState newState) {
        this.state = Objects.requireNonNull(newState);
        if (newState == CabinState.IDLE) {
            this.direction = null;
        }
    }

    /**
     * Задаёт направление движения.
     *
     * @param direction направление (может быть null)
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Перемещает кабину на один этаж в текущем направлении.
     *
     * @throws IllegalStateException если направление не задано или состояние не MOVING
     */
    public void moveOneFloor() {
        if (state != CabinState.MOVING) {
            throw new IllegalStateException("Cannot move when state is " + state);
        }
        if (direction == null) {
            throw new IllegalStateException("Direction is not set");
        }
        currentFloor += (direction == Direction.UP ? 1 : -1);
    }

    /**
     * Мгновенно перемещает кабину на указанный этаж (для тестов/инициализации).
     *
     * @param floor целевой этаж
     */
    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Elevator elevator)) return false;
        return id.equals(elevator.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Elevator[%s, floor=%d, state=%s, dir=%s]",
                id, currentFloor, state, direction);
    }
}
