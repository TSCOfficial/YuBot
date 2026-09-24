package ch.frily.yubot.feature.profile;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public record ProfileMessage(
        Message message,
        Profile profile,
        MessageChannel channel
) {
}
