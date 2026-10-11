package ch.frily.yubot.event;

import ch.frily.yubot.event.listener.*;
import ch.frily.yubot.exception.ExceptionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.Event;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EventRegistry {

    public static EventRegistry instance;

    private final List<IEvent> events = new ArrayList<>();

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
            ReadyListener.getInstance(),
            SlashCommandInteractionListener.getInstance()
    );

    public static EventRegistry getInstance() {
        if (instance == null) {
            instance = new EventRegistry();
        }
        return instance;
    }

    public void register(List<IEvent> listeners) {
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
        List<IEvent> matchingEvents = events.stream().filter(localEvent -> localEvent.getListenerType() == event.getClass()).toList();
        log.info("Listener '{}' triggered event(s): {}.", event.getClass().getSimpleName(), String.join(", ", matchingEvents.stream().map(localEvent -> localEvent.getClass().getSimpleName()).toList()));
        if (!matchingEvents.isEmpty()) {

                matchingEvents.forEach(listener -> {
                    try {
                        listener.execute(event);
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                });
                return;
        }
        throw new NullPointerException(String.format("Event listener for event '%s' not found.", event.getClass().getSimpleName()));
    }

}
