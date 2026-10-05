package ch.frily.yubot.feature.dynamicmsg;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public record DynamicMessage(String name, Message message) {

    /**
     * Retrieve a dynamic message
     * @param registryName the registry name of the dynamic message
     * @param channelId current channel id
     * @param messageId current message id
     * @return a {@link CompletableFuture}, consisting of the {@link DynamicMessage}
     */
    public static CompletableFuture<DynamicMessage> retrieve(String registryName, long channelId, long messageId) {
        long guildId = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getIdLong();

        return EnvResolver.getMessageById(guildId, channelId, messageId)
                .thenApply(Message -> new DynamicMessage(registryName, Message))
                .exceptionally(ExceptionHandler::fail);
    }
}
