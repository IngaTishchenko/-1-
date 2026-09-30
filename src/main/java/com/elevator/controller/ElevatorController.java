package com.elevator.controller;

import com.elevator.exception.InvalidFloorException;
import com.elevator.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Контроллер лифта: принимает заявки, управляет кабиной по стратегии,
 * ведёт журнал и собирает метрики.
 * <p>
 * Время продвигается событиями (переезд этажа, открытие дверей и т.д.).
 */
public final class ElevatorController {

    /** Длительность переезда на один этаж (тики) */
    public static final long TRAVEL_TIME_PER_FLOOR = 5L;
    /** Длительность открытия дверей */
    public static final long DOOR_OPEN_TIME = 2L;
    /** Длительность ожидания посадки */
    public static final long BOARDING_TIME = 3L;

    private final Building building;
    private final Elevator elevator;
    private final SimulationClock clock;
    private final MovementStrategy strategy;
    private final EventLog eventLog;
    private final Metrics metrics;

    /** Ожидающие заявки (мутабельный внутренний список, наружу не отдаём) */
    private final List<FloorRequest> pendingRequests = new ArrayList<>();

    /**
     * Создаёт контроллер.
     *
     * @param building здание
     * @param elevator кабина
     * @param clock    модельные часы
     * @param strategy стратегия выбора цели
     * @param eventLog журнал
     * @param metrics  метрики
     */
    public ElevatorController(
            Building building,
            Elevator elevator,
            SimulationClock clock,
            MovementStrategy strategy,
            EventLog eventLog,
            Metrics metrics) {
        this.building = Objects.requireNonNull(building);
        this.elevator = Objects.requireNonNull(elevator);
        this.clock = Objects.requireNonNull(clock);
        this.strategy = Objects.requireNonNull(strategy);
        this.eventLog = Objects.requireNonNull(eventLog);
        this.metrics = Objects.requireNonNull(metrics);

        building.requireValidFloor(elevator.getCurrentFloor());
    }

    /**
     * Добавляет заявку. Если этаж невалиден — бросает {@link InvalidFloorException}.
     *
     * @param floor     этаж
     * @param direction направление
     * @throws InvalidFloorException если этаж не существует
     */
    public void addRequest(int floor, Direction direction) {
        building.requireValidFloor(floor);
        Objects.requireNonNull(direction, "direction must not be null");

        // На крайних этажах направление фиксировано
        if (floor == building.getMaxFloor() && direction == Direction.UP) {
            throw new IllegalArgumentException("Cannot request UP from top floor " + floor);
        }
        if (floor == building.getMinFloor() && direction == Direction.DOWN) {
            throw new IllegalArgumentException("Cannot request DOWN from bottom floor " + floor);
        }

        FloorRequest request = new FloorRequest(floor, direction, clock.now());
        pendingRequests.add(request);
        eventLog.log(clock.now(),
                String.format("Заявка: этаж %d, %s (ожидает %d)", floor, direction, pendingRequests.size()));
    }

    /**
     * Запускает симуляцию до полной обработки всех заявок (или до maxSteps).
     *
     * @param maxSteps максимальное число шагов (защита от бесконечного цикла)
     * @return количество выполненных шагов
     */
    public int runUntilIdle(int maxSteps) {
        int steps = 0;
        while (steps < maxSteps && (!pendingRequests.isEmpty() || elevator.getState() != CabinState.IDLE)) {
            step();
            steps++;
        }
        return steps;
    }

    /**
     * Один шаг машины состояний.
     */
    public void step() {
        switch (elevator.getState()) {
            case IDLE -> handleIdle();
            case MOVING -> handleMoving();
            case OPENING_DOORS -> handleOpeningDoors();
            case WAITING -> handleWaiting();
        }
    }

    private void handleIdle() {
        Optional<Integer> next = strategy.chooseNextTarget(
                elevator.getCurrentFloor(), null, pendingRequests);
        if (next.isEmpty()) {
            return; // действительно idle
        }
        int target = next.get();
        Direction dir = target > elevator.getCurrentFloor() ? Direction.UP : Direction.DOWN;
        if (target == elevator.getCurrentFloor()) {
            // Уже на месте — сразу открываем двери
            elevator.setState(CabinState.OPENING_DOORS);
            eventLog.log(clock.now(), "Кабина уже на этаже " + target + ", открывает двери");
        } else {
            elevator.setDirection(dir);
            elevator.setState(CabinState.MOVING);
            eventLog.log(clock.now(),
                    String.format("Кабина начинает движение %s к этажу %d", dir, target));
        }
    }

    private void handleMoving() {
        // Определяем, нужно ли остановиться на текущем этаже
        boolean shouldStop = shouldStopAtCurrentFloor();
        if (shouldStop) {
            elevator.setState(CabinState.OPENING_DOORS);
            metrics.recordStop();
            eventLog.log(clock.now(),
                    String.format("Кабина прибыла на этаж %d, открывает двери", elevator.getCurrentFloor()));
            return;
        }

        // Двигаемся на один этаж
        clock.advance(TRAVEL_TIME_PER_FLOOR);
        elevator.moveOneFloor();
        eventLog.log(clock.now(),
                String.format("Кабина на этаже %d (направление %s)", elevator.getCurrentFloor(), elevator.getDirection()));

        // Проверяем остановку после перемещения
        if (shouldStopAtCurrentFloor()) {
            elevator.setState(CabinState.OPENING_DOORS);
            metrics.recordStop();
            eventLog.log(clock.now(),
                    String.format("Кабина прибыла на этаж %d, открывает двери", elevator.getCurrentFloor()));
        }
    }

    private boolean shouldStopAtCurrentFloor() {
        int floor = elevator.getCurrentFloor();
        Direction dir = elevator.getDirection();

        // Есть заявка на этот этаж?
        boolean hasRequestHere = pendingRequests.stream()
                .anyMatch(r -> r.floor() == floor);

        if (!hasRequestHere) {
            // Может, это конечная цель стратегии?
            Optional<Integer> next = strategy.chooseNextTarget(floor, dir, pendingRequests);
            return next.isPresent() && next.get() == floor;
        }
        return true;
    }

    private void handleOpeningDoors() {
        clock.advance(DOOR_OPEN_TIME);
        elevator.setState(CabinState.WAITING);
        eventLog.log(clock.now(),
                String.format("Двери открыты на этаже %d, ждём посадки", elevator.getCurrentFloor()));
    }

    private void handleWaiting() {
        clock.advance(BOARDING_TIME);
        int floor = elevator.getCurrentFloor();

        // Обслуживаем все заявки на этом этаже
        Iterator<FloorRequest> it = pendingRequests.iterator();
        while (it.hasNext()) {
            FloorRequest r = it.next();
            if (r.floor() == floor) {
                long wait = clock.now() - r.time();
                metrics.recordWaitingTime(wait);
                eventLog.log(clock.now(),
                        String.format("Обслужена заявка этаж %d %s (ожидание %d тиков)",
                                r.floor(), r.direction(), wait));
                it.remove();
            }
        }

        // Выбираем следующую цель
        Optional<Integer> next = strategy.chooseNextTarget(
                floor, elevator.getDirection(), pendingRequests);

        if (next.isEmpty()) {
            elevator.setState(CabinState.IDLE);
            elevator.setDirection(null);
            eventLog.log(clock.now(), "Нет больше заявок, кабина простаивает");
        } else {
            int target = next.get();
            if (target == floor) {
                // Теоретически не должно, но на всякий случай
                elevator.setState(CabinState.OPENING_DOORS);
            } else {
                Direction newDir = target > floor ? Direction.UP : Direction.DOWN;
                elevator.setDirection(newDir);
                elevator.setState(CabinState.MOVING);
                eventLog.log(clock.now(),
                        String.format("Кабина продолжает движение %s к этажу %d", newDir, target));
            }
        }
    }

    /**
     * @return здание
     */
    public Building getBuilding() {
        return building;
    }

    /**
     * @return кабина
     */
    public Elevator getElevator() {
        return elevator;
    }

    /**
     * @return часы
     */
    public SimulationClock getClock() {
        return clock;
    }

    /**
     * @return стратегия
     */
    public MovementStrategy getStrategy() {
        return strategy;
    }

    /**
     * @return журнал
     */
    public EventLog getEventLog() {
        return eventLog;
    }

    /**
     * @return метрики
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Снимок ожидающих заявок (defensive copy).
     *
     * @return неизменяемый список
     */
    public List<FloorRequest> getPendingRequests() {
        return Collections.unmodifiableList(new ArrayList<>(pendingRequests));
    }

    /**
     * Количество ожидающих заявок.
     *
     * @return размер очереди
     */
    public int pendingCount() {
        return pendingRequests.size();
    }
}
