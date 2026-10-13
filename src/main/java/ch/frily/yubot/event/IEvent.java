package ch.frily.yubot.event;

import ch.frily.yubot.Client;
import net.dv8tion.jda.api.events.Event;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Events are used to define an action for a specific event-type.<br>
 * Using the {@link EventRegistry#getLISTENERS() Listeners}, registered via {@link Client#setup()}, the registered {@link EventRegistry#register(List) events} are dispatched and execute their set actions.
 * @param <T>
 */
public interface IEvent<T extends Event> {

    Class<? extends Event> getListenerType();

    /**
     * The code that should be executed when the event class gets triggered
     * <p>
     *     To be able to use the event specific methods, the generic event needs to be cast to the specific event class.
     *     This is possible due that the listener is set for that event specificlly.<br>
     *     Add the following cast to be able to use the methods:
     *     <pre><code>
     *         TheSpecificEvent castEvent = (TheSpecificEvent) event;
     *         event.getMember(); // ex.: get the member of the event
     *     </code></pre>
     * </p>
     * @param event
     */
    <T extends Event> void execute(T event) throws SQLException, IOException;
}
