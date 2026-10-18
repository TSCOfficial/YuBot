package ch.frily.yubot.event.listener;

import ch.frily.yubot.event.EventRegistry;
import ch.frily.yubot.exception.ExceptionHandler;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildMemberRoleAddListener extends ListenerAdapter {

    private static GuildMemberRoleAddListener instance;

    public static GuildMemberRoleAddListener getInstance() {
        if (instance == null) {
            instance = new GuildMemberRoleAddListener();
        }
        return instance;
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        try {
            EventRegistry.getInstance().dispatchEvent(event);
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

}
