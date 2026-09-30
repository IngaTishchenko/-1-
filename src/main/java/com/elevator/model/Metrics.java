package com.elevator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Метрики прогона симуляции: времена ожидания и число остановок.
 */
public final class Metrics {

    private final List<Long> waitingTimes = new ArrayList<>();
    private int stopCount;

    /**
     * Регистрирует время ожидания одной заявки (от момента нажатия до обслуживания).
     *
     * @param waitingTime время ожидания в тиках (≥ 0)
     * @throws IllegalArgumentException если waitingTime &lt; 0
     */
    public void recordWaitingTime(long waitingTime) {
        if (waitingTime < 0) {
            throw new IllegalArgumentException("waitingTime must be non-negative: " + waitingTime);
        }
        waitingTimes.add(waitingTime);
    }

    /**
     * Увеличивает счётчик остановок.
     */
    public void recordStop() {
        stopCount++;
    }

    /**
     * Среднее время ожидания.
     *
     * @return среднее, или 0.0 если нет данных
     */
    public double averageWaitingTime() {
        if (waitingTimes.isEmpty()) {
            return 0.0;
        }
        long sum = 0;
        for (long t : waitingTimes) {
            sum += t;
        }
        return (double) sum / waitingTimes.size();
    }

    /**
     * Максимальное время ожидания.
     *
     * @return максимум, или 0 если нет данных
     */
    public long maxWaitingTime() {
        if (waitingTimes.isEmpty()) {
            return 0L;
        }
        long max = 0;
        for (long t : waitingTimes) {
            if (t > max) {
                max = t;
            }
        }
        return max;
    }

    /**
     * Количество зарегистрированных остановок.
     *
     * @return число остановок
     */
    public int getStopCount() {
        return stopCount;
    }

    /**
     * Количество обслуженных заявок (по которым записано время ожидания).
     *
     * @return число заявок
     */
    public int getServedRequestCount() {
        return waitingTimes.size();
    }

    /**
     * Снимок всех времён ожидания (defensive copy).
     *
     * @return неизменяемый список
     */
    public List<Long> getWaitingTimes() {
        return Collections.unmodifiableList(new ArrayList<>(waitingTimes));
    }

    /**
     * Сбрасывает все метрики.
     */
    public void reset() {
        waitingTimes.clear();
        stopCount = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "Metrics{served=%d, stops=%d, avgWait=%.2f, maxWait=%d}",
                getServedRequestCount(), stopCount, averageWaitingTime(), maxWaitingTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metrics metrics)) return false;
        return stopCount == metrics.stopCount && waitingTimes.equals(metrics.waitingTimes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waitingTimes, stopCount);
    }
}
