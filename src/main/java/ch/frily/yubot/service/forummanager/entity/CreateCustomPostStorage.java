package ch.frily.yubot.service.forummanager.entity;

import ch.frily.yubot.container.StaticContainerRegistry;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Saves the data for creating a custom post, when an error occurs during the creation process.
 * @param forumChannel
 * @param title
 * @param content
 */
public record CreateCustomPostStorage(
        @NotNull ForumChannel forumChannel,
        @NotNull String title,
        String content,
        String containerEmbedKey
) {
    public static String KEY = "create-custom-post";
}
