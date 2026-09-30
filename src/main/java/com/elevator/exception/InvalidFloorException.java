package com.elevator.exception;

/**
 * Исключение при попытке создать заявку или обратиться к несуществующему этажу.
 */
public class InvalidFloorException extends RuntimeException {

    private final int floor;
    private final int minFloor;
    private final int maxFloor;

    /**
     * Создаёт исключение с информацией о допустимом диапазоне.
     *
     * @param floor    запрошенный этаж
     * @param minFloor минимальный этаж здания
     * @param maxFloor максимальный этаж здания
     */
    public InvalidFloorException(int floor, int minFloor, int maxFloor) {
        super(String.format(
                "Этаж %d не существует. Допустимый диапазон: [%d, %d]",
                floor, minFloor, maxFloor));
        this.floor = floor;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }

    /**
     * @return запрошенный (неверный) этаж
     */
    public int getFloor() {
        return floor;
    }

    /**
     * @return минимальный этаж здания
     */
    public int getMinFloor() {
        return minFloor;
    }

    /**
     * @return максимальный этаж здания
     */
    public int getMaxFloor() {
        return maxFloor;
    }
}
