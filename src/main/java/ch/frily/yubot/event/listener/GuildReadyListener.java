package ch.frily.yubot.event.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class GuildReadyListener extends ListenerAdapter {

    private static GuildReadyListener instance;

    public static GuildReadyListener getInstance() {
        if (instance == null) {
            instance = new GuildReadyListener();
        }
        return instance;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        // syncs the cache data
        event.getGuild().loadMembers().onSuccess(_ -> {
            log.info("Loaded members for guild '{}'.", event.getGuild().getName());
            int cached = event.getGuild().getMembers().size();
            int total  = event.getGuild().getMemberCount();

            log.debug("{}: Cached members: {} / Total members: {}", event.getGuild(), cached, total);
        });
    }
}
