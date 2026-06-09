package ch.frily.yubot.embed;

import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * This is a list of Embeds that can be used statically via the send embed command
 */
public enum StaticEmbedRegistry {

    NONE(null);

    private final MessageEmbed embed;

    StaticEmbedRegistry(MessageEmbed embed) {
        this.embed = embed;
    }

    public MessageEmbed getEmbed() {
        return embed;
    }
}
