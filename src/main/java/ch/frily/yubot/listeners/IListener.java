package ch.frily.yubot.listeners;

import net.dv8tion.jda.api.events.Event;

public interface IListener<T extends Event> {

    ListenerType getListenerType();

    <T extends Event> void execute(T event);
}
