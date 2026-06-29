package ch.frily.yubot.feature;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;

import java.util.concurrent.CompletableFuture;

public record DynamicMessage(String name, Message message) {

    /**
     * Retrieve a dynamic message
     * @param name the registry name of the dynamic message
     * @param channelId current channel id
     * @param messageId current message id
     * @return
     */
    public static CompletableFuture<DynamicMessage> retrieve(String name, long channelId, long messageId) {
        long guildId = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getIdLong();

        return EnvResolver.getMessageById(guildId, channelId, messageId)
                .thenApply(Message -> new DynamicMessage(name, Message))
                .exceptionally(ExceptionHandler::fail);
    }
}
