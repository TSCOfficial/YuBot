package ch.frily.yubot.embed;

import ch.frily.yubot.embed.teamlist.TeamlistEmbed;
import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.function.Supplier;

/**
 * This is a list of Embeds that can be used statically via the send embed command
 */
public enum StaticEmbedRegistry {

    TEAMLIST(() -> new TeamlistEmbed().build());

    @Getter
    private final Supplier<MessageEmbed> embedSupplier;

    StaticEmbedRegistry(Supplier<MessageEmbed> embedSupplier) {
        this.embedSupplier = embedSupplier;
    }

    public MessageEmbed getEmbed() {
        return embedSupplier.get();
    }

}
