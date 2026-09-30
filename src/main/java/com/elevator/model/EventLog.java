package com.elevator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Журнал событий симуляции с временными метками.
 * <p>
 * Хранит неизменяемые записи. Возвращает defensive copy списка.
 */
public final class EventLog {

    /**
     * Одна запись журнала.
     *
     * @param time    модельное время
     * @param message описание события
     */
    public record Entry(long time, String message) {
        public Entry {
            Objects.requireNonNull(message, "message must not be null");
        }

        @Override
        public String toString() {
            return String.format("[%5d] %s", time, message);
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    /**
     * Добавляет событие в журнал.
     *
     * @param time    время события
     * @param message описание
     */
    public void log(long time, String message) {
        entries.add(new Entry(time, message));
    }

    /**
     * Возвращает неизменяемый снимок всех записей.
     *
     * @return список записей
     */
    public List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    /**
     * Количество записей.
     *
     * @return размер журнала
     */
    public int size() {
        return entries.size();
    }

    /**
     * Очищает журнал.
     */
    public void clear() {
        entries.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EventLog (").append(entries.size()).append(" entries):\n");
        for (Entry e : entries) {
            sb.append("  ").append(e).append('\n');
        }
        return sb.toString();
    }
}
