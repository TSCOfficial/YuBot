package ch.frily.yubot.listeners;

import lombok.Getter;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Used to map the listener code to the listener event itself
 * <p>
 *     The Enum uses JDA's event classes, extending on {@link Event}
 * </p>
 */
public enum ListenerType {
    GUILD_MEMBER_UPDATE(GuildMemberRoleAddEvent.class),
    MESSAGE_RECEIVED(MessageReceivedEvent.class);

    @Getter
    final Class<? extends Event> listener;

    ListenerType(Class<? extends Event> instance) {
        listener = instance;
    }
}
