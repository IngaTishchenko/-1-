package com.elevator.controller;

import com.elevator.model.Direction;
import com.elevator.model.FloorRequest;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * Стратегия «сбор попутных»: кабина идёт в одну сторону, пока есть цели
 * в этом направлении, затем разворачивается.
 * <p>
 * Это основной алгоритм базового уровня.
 */
public final class CollectingStrategy implements MovementStrategy {

    @Override
    public Optional<Integer> chooseNextTarget(
            int currentFloor,
            Direction currentDirection,
            Collection<FloorRequest> pendingRequests) {

        if (pendingRequests.isEmpty()) {
            return Optional.empty();
        }

        // Если направления нет — выбираем ближайшую заявку и задаём направление
        if (currentDirection == null) {
            return pendingRequests.stream()
                    .min(Comparator.comparingInt(r -> Math.abs(r.floor() - currentFloor)))
                    .map(FloorRequest::floor);
        }

        // Цели «по пути» в текущем направлении
        var sameDirection = pendingRequests.stream()
                .filter(r -> isInDirection(currentFloor, r.floor(), currentDirection))
                .toList();

        if (!sameDirection.isEmpty()) {
            // Ближайшая в направлении движения
            Comparator<FloorRequest> byDistance = currentDirection == Direction.UP
                    ? Comparator.comparingInt(FloorRequest::floor)
                    : Comparator.comparingInt(FloorRequest::floor).reversed();
            return sameDirection.stream()
                    .min(byDistance)
                    .map(FloorRequest::floor);
        }

        // Нет целей в текущем направлении — берём ближайшую в любую сторону
        // (контроллер потом развернётся)
        return pendingRequests.stream()
                .min(Comparator.comparingInt(r -> Math.abs(r.floor() - currentFloor)))
                .map(FloorRequest::floor);
    }

    private static boolean isInDirection(int from, int to, Direction dir) {
        return dir == Direction.UP ? to > from : to < from;
    }

    @Override
    public String name() {
        return "Collecting (попутный сбор)";
    }
}
