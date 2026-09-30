package com.elevator.controller;

import com.elevator.model.Direction;
import com.elevator.model.FloorRequest;

import java.util.Collection;
import java.util.Optional;

/**
 * Стратегия выбора следующей цели для кабины.
 * <p>
 * Полиморфный метод {@link #chooseNextTarget} позволяет подменять алгоритм
 * без изменения контроллера.
 */
public interface MovementStrategy {

    /**
     * Выбирает следующую цель (этаж) для кабины.
     *
     * @param currentFloor     текущий этаж кабины
     * @param currentDirection текущее направление (может быть {@code null}, если IDLE)
     * @param pendingRequests  ожидающие заявки
     * @return этаж следующей цели или empty, если целей нет
     */
    Optional<Integer> chooseNextTarget(
            int currentFloor,
            Direction currentDirection,
            Collection<FloorRequest> pendingRequests);

    /**
     * Имя стратегии (для отчётов).
     *
     * @return человекочитаемое имя
     */
    String name();
}
