package ch.frily.yubot.listeners;

import net.dv8tion.jda.api.events.Event;

import java.util.ArrayList;
import java.util.List;

public class ListenerRegistry {

    public static ListenerRegistry instance;

    private final List<IListener> listeners = new ArrayList<>();

    public static ListenerRegistry getInstance() {
        if (instance == null) {
            instance = new ListenerRegistry();
        }
        return instance;
    }

    public void register(List<IListener> listener) {
        listeners.addAll(listener);
    }

    public void dispatchEvent(Event event) {
        List<IListener> matchingListeners = listeners.stream().filter(listener -> listener.getListenerType().listener == event.getClass()).toList();
        if (!matchingListeners.isEmpty()) {
            matchingListeners.forEach(listener -> listener.execute(event));
        }
        throw new NullPointerException(String.format("Event listener for event '%s' not found.", event.getClass().getSimpleName()));
    }

}
