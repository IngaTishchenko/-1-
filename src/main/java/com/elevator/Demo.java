package com.elevator;

import com.elevator.controller.CollectingStrategy;
import com.elevator.controller.ElevatorController;
import com.elevator.controller.MovementStrategy;
import com.elevator.controller.SimpleOrderStrategy;
import com.elevator.exception.InvalidFloorException;
import com.elevator.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Консольная демонстрация симуляции лифта (базовый уровень).
 * <p>
 * Наполняет модель ≥ 30 объектами и последовательно показывает все операции.
 */
public class Demo {

    public static void main(String[] args) {
        System.out.println("=== Симуляция лифта (базовый уровень) ===\n");

        // --- Создание модели ---
        Building building = new Building(1, 10);
        System.out.println("1. Здание: " + building + " (" + building.floorCount() + " этажей)");

        Elevator elevator = new Elevator("A1", 1);
        System.out.println("2. Кабина: " + elevator);

        SimulationClock clock = new SimulationClock();
        System.out.println("3. Модельные часы: " + clock);

        EventLog log = new EventLog();
        Metrics metrics = new Metrics();

        MovementStrategy strategy = new CollectingStrategy();
        System.out.println("4. Стратегия: " + strategy.name());

        ElevatorController controller = new ElevatorController(
                building, elevator, clock, strategy, log, metrics);
        System.out.println("5. Контроллер создан\n");

        // --- Демонстрация исключения ---
        System.out.println("--- Проверка исключения InvalidFloorException ---");
        try {
            controller.addRequest(99, Direction.UP);
        } catch (InvalidFloorException e) {
            System.out.println("Поймано: " + e.getMessage());
            System.out.println("  floor=" + e.getFloor() + ", range=[" + e.getMinFloor() + "," + e.getMaxFloor() + "]");
        }
        System.out.println();

        // --- Генерация ≥ 30 заявок ---
        System.out.println("--- Добавление 35 заявок ---");
        List<FloorRequest> generated = generateRequests(35, building, clock);
        for (FloorRequest r : generated) {
            // Устанавливаем время заявки через часы (для реалистичности добавляем с небольшим сдвигом)
            clock.advance(1); // небольшая пауза между нажатиями
            try {
                controller.addRequest(r.floor(), r.direction());
            } catch (IllegalArgumentException ex) {
                // крайние этажи — пропускаем некорректное направление
                System.out.println("  пропущена некорректная заявка: " + r + " (" + ex.getMessage() + ")");
            }
        }
        System.out.println("Добавлено заявок в очередь: " + controller.pendingCount());
        System.out.println("Всего объектов FloorRequest создано: " + generated.size());
        System.out.println();

        // --- Запуск симуляции ---
        System.out.println("--- Запуск симуляции (CollectingStrategy) ---");
        int steps = controller.runUntilIdle(10_000);
        System.out.println("Выполнено шагов: " + steps);
        System.out.println("Финальное состояние кабины: " + elevator);
        System.out.println("Время симуляции: " + clock.now() + " тиков");
        System.out.println();

        // --- Метрики ---
        System.out.println("--- Метрики ---");
        System.out.println(metrics);
        System.out.printf("  Среднее время ожидания: %.2f тиков%n", metrics.averageWaitingTime());
        System.out.println("  Максимальное время ожидания: " + metrics.maxWaitingTime() + " тиков");
        System.out.println("  Число остановок: " + metrics.getStopCount());
        System.out.println("  Обслужено заявок: " + metrics.getServedRequestCount());
        System.out.println();

        // --- Журнал (первые и последние записи) ---
        System.out.println("--- Журнал событий (всего " + log.size() + ") ---");
        var entries = log.getEntries();
        int show = Math.min(15, entries.size());
        System.out.println("Первые " + show + ":");
        for (int i = 0; i < show; i++) {
            System.out.println("  " + entries.get(i));
        }
        if (entries.size() > 30) {
            System.out.println("  ...");
            System.out.println("Последние 10:");
            for (int i = entries.size() - 10; i < entries.size(); i++) {
                System.out.println("  " + entries.get(i));
            }
        }
        System.out.println();

        // --- Демонстрация второй стратегии (для сравнения) ---
        System.out.println("--- Сравнение со SimpleOrderStrategy ---");
        runComparison(building);
    }

    private static List<FloorRequest> generateRequests(int count, Building building, SimulationClock clock) {
        Random rnd = new Random(42); // фиксированный seed для воспроизводимости
        List<FloorRequest> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int floor = building.getMinFloor() + rnd.nextInt(building.floorCount());
            Direction dir;
            if (floor == building.getMaxFloor()) {
                dir = Direction.DOWN;
            } else if (floor == building.getMinFloor()) {
                dir = Direction.UP;
            } else {
                dir = rnd.nextBoolean() ? Direction.UP : Direction.DOWN;
            }
            list.add(new FloorRequest(floor, dir, clock.now() + i));
        }
        return list;
    }

    private static void runComparison(Building building) {
        // Одинаковый набор заявок для обеих стратегий
        List<int[]> rawRequests = List.of(
                new int[]{3, 1},  // floor, dir (1=UP, 0=DOWN)
                new int[]{7, 0},
                new int[]{2, 1},
                new int[]{9, 0},
                new int[]{5, 1},
                new int[]{4, 0},
                new int[]{8, 1},
                new int[]{1, 1},
                new int[]{6, 0},
                new int[]{10, 0}
        );

        System.out.println("Набор из " + rawRequests.size() + " заявок:");

        Metrics m1 = runWithStrategy(building, new CollectingStrategy(), rawRequests);
        Metrics m2 = runWithStrategy(building, new SimpleOrderStrategy(), rawRequests);

        System.out.println("\nТаблица сравнения:");
        System.out.printf("%-30s %12s %12s %10s%n", "Стратегия", "Avg wait", "Max wait", "Stops");
        System.out.printf("%-30s %12.2f %12d %10d%n",
                "Collecting", m1.averageWaitingTime(), m1.maxWaitingTime(), m1.getStopCount());
        System.out.printf("%-30s %12.2f %12d %10d%n",
                "SimpleOrder", m2.averageWaitingTime(), m2.maxWaitingTime(), m2.getStopCount());
    }

    private static Metrics runWithStrategy(Building building, MovementStrategy strategy, List<int[]> raw) {
        Elevator elev = new Elevator("cmp", 1);
        SimulationClock clk = new SimulationClock();
        EventLog log = new EventLog();
        Metrics m = new Metrics();
        ElevatorController ctrl = new ElevatorController(building, elev, clk, strategy, log, m);

        for (int[] r : raw) {
            Direction d = r[1] == 1 ? Direction.UP : Direction.DOWN;
            ctrl.addRequest(r[0], d);
            clk.advance(2);
        }
        ctrl.runUntilIdle(5000);
        System.out.println("  " + strategy.name() + " → " + m + ", time=" + clk.now());
        return m;
    }
}
