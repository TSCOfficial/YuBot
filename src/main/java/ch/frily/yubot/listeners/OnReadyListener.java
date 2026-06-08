package ch.frily.yubot.listeners;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
public class OnReadyListener extends ListenerAdapter {

    private static OnReadyListener instance;

    public static OnReadyListener getInstance() {
        if (instance == null) {
            instance = new OnReadyListener();
        }
        return instance;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().loadMembers().onSuccess(members -> {
            log.info("Loaded members for guild '{}'.", event.getGuild().getName());
            int cached = event.getGuild().getMembers().size();
            int total  = event.getGuild().getMemberCount();

            log.debug("{}: Cached members: {} / Total members: {}", event.getGuild(), cached, total);
        });
    }

    @Override
    public void onReady(ReadyEvent event) {
        log.debug("ON READY");
    }
}
