package com.elevator.model;

import com.elevator.exception.InvalidFloorException;

/**
 * Здание с диапазоном этажей.
 */
public final class Building {

    private final int minFloor;
    private final int maxFloor;

    /**
     * Создаёт здание с этажами от minFloor до maxFloor включительно.
     *
     * @param minFloor минимальный этаж
     * @param maxFloor максимальный этаж
     * @throws IllegalArgumentException если maxFloor &lt; minFloor
     */
    public Building(int minFloor, int maxFloor) {
        if (maxFloor < minFloor) {
            throw new IllegalArgumentException(
                    "maxFloor (" + maxFloor + ") must be >= minFloor (" + minFloor + ")");
        }
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }

    /**
     * @return минимальный этаж
     */
    public int getMinFloor() {
        return minFloor;
    }

    /**
     * @return максимальный этаж
     */
    public int getMaxFloor() {
        return maxFloor;
    }

    /**
     * Количество этажей.
     *
     * @return max - min + 1
     */
    public int floorCount() {
        return maxFloor - minFloor + 1;
    }

    /**
     * Проверяет, существует ли этаж.
     *
     * @param floor номер этажа
     * @return true, если этаж в диапазоне
     */
    public boolean isValidFloor(int floor) {
        return floor >= minFloor && floor <= maxFloor;
    }

    /**
     * Проверяет этаж и бросает исключение, если он невалиден.
     *
     * @param floor номер этажа
     * @throws InvalidFloorException если этаж вне диапазона
     */
    public void requireValidFloor(int floor) {
        if (!isValidFloor(floor)) {
            throw new InvalidFloorException(floor, minFloor, maxFloor);
        }
    }

    @Override
    public String toString() {
        return String.format("Building[%d..%d]", minFloor, maxFloor);
    }
}
