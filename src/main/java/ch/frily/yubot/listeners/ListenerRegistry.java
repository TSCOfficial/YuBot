package ch.frily.yubot.listeners;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.Event;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ListenerRegistry {

    public static ListenerRegistry instance;

    private final List<IListener> listeners = new ArrayList<>();

    public static ListenerRegistry getInstance() {
        if (instance == null) {
            instance = new ListenerRegistry();
        }
        return instance;
    }

    public void register(List<IListener> listeners) {
        listeners.forEach(listener -> {
            this.listeners.add(listener);
            log.info("Registered eventlistener '{}'", listener.getClass().getSimpleName());
        });
    }

    public void dispatchEvent(Event event) {
        List<IListener> matchingListeners = listeners.stream().filter(listener -> {
            log.info("Listener: '{}'", listener.getListenerType().getSimpleName());
            log.info("event: '{}'", event.getClass().getSimpleName());
            return listener.getListenerType() == event.getClass();
        }).toList();
        if (!matchingListeners.isEmpty()) {
            log.info("Found matching listeners {} for event {}", matchingListeners, event);
            matchingListeners.forEach(listener -> listener.execute(event));
            return;
        }
        throw new NullPointerException(String.format("Event listener for event '%s' not found.", event.getClass().getSimpleName()));
    }

}
