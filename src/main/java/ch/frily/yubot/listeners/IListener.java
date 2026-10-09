package ch.frily.yubot.listeners;

import net.dv8tion.jda.api.events.Event;

public interface IListener<T extends Event> {

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
     * @param <T>
     */
    <T extends Event> void execute(T event);
}
