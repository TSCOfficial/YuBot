package ch.frily.yubot.event;

import ch.frily.yubot.event.listener.*;
import ch.frily.yubot.exception.ExceptionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EventRegistry extends ListenerAdapter {

    public static EventRegistry instance;

    private final List<IEvent<? extends Event>> events = new ArrayList<>();

    /**
     * the list of available listeners for the events
     * <p>
     *     It's get method need to be called at the jda builder to connect the listeners
     * </p>
     **/
    @Getter
    private static final List<Object> LISTENERS = List.of(
            ButtonInteractionListener.getInstance(),
            CommandAutoCompleteInteractionListener.getInstance(),
            GenericContextInteractionListener.getInstance(),
            GenericSelectMenuInteraction.getInstance(),
            GuildMemberRemoveListener.getInstance(),
            GuildMemberRoleAddListener.getInstance(),
            GuildMemberRoleRemoveListener.getInstance(),
            GuildReadyListener.getInstance(),
            MessageReceivedListener.getInstance(),
            ModalInteractionListener.getInstance(),
            SlashCommandInteractionListener.getInstance()
    );

    public static EventRegistry getInstance() {
        if (instance == null) {
            instance = new EventRegistry();
        }
        return instance;
    }

    public void register(List<IEvent<? extends Event>> listeners) {
        listeners.forEach(listener -> {
            this.events.add(listener);
            log.info("Registered event '{}' for listener '{}'", listener.getClass().getSimpleName(), listener.getListenerType().getSimpleName());
        });
    }

    /**
     * Dispatches the Discord event to the appropriate local {@link IEvent}
     * @param event the event triggered by discord
     */
    public void dispatchEvent(Event event) {
        List<IEvent<? extends Event>> matchingEvents = events.stream().filter(localEvent -> localEvent.getListenerType() == event.getClass()).toList();
        log.info("Listener '{}' dispatching {} event(s): {}.", event.getClass().getSimpleName(), matchingEvents.size(), String.join(", ", matchingEvents.stream().map(localEvent -> localEvent.getClass().getSimpleName()).toList()));
        if (!matchingEvents.isEmpty()) {

                matchingEvents.forEach(listener -> {
                    try {
                        listener.execute(event);
                    } catch (Exception e) {
                        log.error("Error while executing event '{}' from listener '{}'", event.getClass().getSimpleName(), listener);
                        ExceptionHandler.handle(e);
                    }
                });
                return;
        }
        throw new NullPointerException(String.format("Event listener for event '%s' not found.", event.getClass().getSimpleName()));
    }

}
