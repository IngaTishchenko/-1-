package com.elevator.model;

/**
 * Модельные часы. Время продвигается событиями, а не {@code Thread.sleep}.
 * <p>
 * Единица времени — абстрактный «тик». Длительности операций задаются
 * в тиках (переезд на один этаж, открытие дверей и т.д.).
 */
public final class SimulationClock {

    private long currentTime;

    /**
     * Создаёт часы, начинающиеся с нуля.
     */
    public SimulationClock() {
        this(0L);
    }

    /**
     * Создаёт часы с заданным начальным временем.
     *
     * @param initialTime начальное время (неотрицательное)
     * @throws IllegalArgumentException если initialTime &lt; 0
     */
    public SimulationClock(long initialTime) {
        if (initialTime < 0) {
            throw new IllegalArgumentException("initialTime must be non-negative");
        }
        this.currentTime = initialTime;
    }

    /**
     * Текущее модельное время.
     *
     * @return текущий тик
     */
    public long now() {
        return currentTime;
    }

    /**
     * Продвигает время на указанное количество тиков.
     *
     * @param delta количество тиков (должно быть ≥ 0)
     * @throws IllegalArgumentException если delta &lt; 0
     */
    public void advance(long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta must be non-negative: " + delta);
        }
        currentTime += delta;
    }

    /**
     * Устанавливает время в конкретное значение (для тестов).
     *
     * @param time новое время (≥ 0)
     */
    public void set(long time) {
        if (time < 0) {
            throw new IllegalArgumentException("time must be non-negative");
        }
        this.currentTime = time;
    }

    @Override
    public String toString() {
        return "t=" + currentTime;
    }
}
