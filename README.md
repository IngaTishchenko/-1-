# Симуляция лифта (Elevator Simulation)

Модель предметной области «Симуляция лифта» на Java 21. Базовый уровень.

## Модель

- **Building** — здание с диапазоном этажей `[minFloor, maxFloor]`.
- **Elevator** — кабина: id, текущий этаж, состояние (`CabinState`), направление (`Direction`).
- **FloorRequest** — заявка (record): этаж, направление, момент нажатия.
- **SimulationClock** — модельные часы (время двигается событиями).
- **EventLog** — журнал событий с временными метками.
- **Metrics** — среднее/макс. время ожидания, число остановок.
- **ElevatorController** — контроллер: принимает заявки, управляет машиной состояний кабины, использует стратегию выбора цели.
- **MovementStrategy** — интерфейс стратегии (полиморфизм):
  - `CollectingStrategy` — сбор попутных заявок (основной алгоритм базового уровня);
  - `SimpleOrderStrategy` — по порядку поступления (FIFO).

### Машина состояний кабины

```
IDLE → MOVING → OPENING_DOORS → WAITING → (MOVING | IDLE)
```

Длительности (тики):
- переезд на 1 этаж: 5
- открытие дверей: 2
- посадка: 3

### Исключение

`InvalidFloorException` — заявка на несуществующий этаж.

### Требования Java 21

- `record` для `FloorRequest` и `EventLog.Entry`
- `switch` как выражение / pattern matching в контроллере
- `var` для локальных переменных
- enum с полями и методами (`CabinState`, `Direction`)

## Сборка и запуск

```bash
# Сборка
mvn clean package

# Запуск демонстрации
mvn exec:java -Dexec.mainClass="com.elevator.Demo"
# или
java -jar target/elevator-sim-1.0-SNAPSHOT.jar

# Тесты
mvn test

# Javadoc
mvn javadoc:javadoc
# отчёт: target/site/apidocs/index.html
```

## Структура пакетов

```
com.elevator
├── Demo                    — консольная демонстрация
├── model                   — Building, Elevator, FloorRequest, CabinState,
│                             Direction, SimulationClock, EventLog, Metrics
├── controller              — ElevatorController, MovementStrategy,
│                             CollectingStrategy, SimpleOrderStrategy
└── exception               — InvalidFloorException
```

## Демонстрация

`Demo` создаёт здание (1–10), кабину, ≥ 35 заявок, запускает симуляцию со стратегией «попутный сбор», выводит метрики и журнал, затем сравнивает две стратегии на одном наборе заявок.
