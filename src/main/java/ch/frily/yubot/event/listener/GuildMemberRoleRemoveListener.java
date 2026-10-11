package ch.frily.yubot.event.listener;

import ch.frily.yubot.exception.ExceptionHandler;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildMemberRoleRemoveListener extends ListenerAdapter {

    private static GuildMemberRoleRemoveListener instance;

    public static GuildMemberRoleRemoveListener getInstance() {
        if (instance == null) {
            instance = new GuildMemberRoleRemoveListener();
        }
        return instance;
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        try {

        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }
}
