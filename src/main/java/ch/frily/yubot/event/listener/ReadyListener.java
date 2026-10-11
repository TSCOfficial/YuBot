package ch.frily.yubot.event.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ReadyListener extends ListenerAdapter {

    private static ReadyListener instance;

    public static ReadyListener getInstance() {
        if (instance == null) {
            instance = new ReadyListener();
        }
        return instance;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.debug("ON READY");
    }
}
