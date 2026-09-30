package com.elevator.controller;

import com.elevator.model.Direction;
import com.elevator.model.FloorRequest;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * Простая стратегия «по порядку поступления» (FIFO по времени заявки).
 * Игнорирует попутность — всегда берёт самую раннюю заявку.
 */
public final class SimpleOrderStrategy implements MovementStrategy {

    @Override
    public Optional<Integer> chooseNextTarget(
            int currentFloor,
            Direction currentDirection,
            Collection<FloorRequest> pendingRequests) {

        return pendingRequests.stream()
                .min(Comparator.comparingLong(FloorRequest::time)
                        .thenComparingInt(r -> Math.abs(r.floor() - currentFloor)))
                .map(FloorRequest::floor);
    }

    @Override
    public String name() {
        return "SimpleOrder (по порядку)";
    }
}
