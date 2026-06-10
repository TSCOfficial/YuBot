package ch.frily.yubot.embed;

import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * This is a list of Embeds that can be used statically via the send embed command
 */
public enum StaticEmbedRegistry {

    NONE(null);

    @Getter
    private final MessageEmbed embed;

    StaticEmbedRegistry(MessageEmbed embed) {
        this.embed = embed;
    }

}
