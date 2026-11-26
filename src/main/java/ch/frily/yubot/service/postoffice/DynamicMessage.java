package ch.frily.yubot.service.postoffice;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.EnvKey;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.util.MessageUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.Objects;
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
        Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();
        GuildChannel guildChannel = guild.getGuildChannelById(channelId);
        if (Objects.isNull(guildChannel)) {
            throw new NotFoundException("Channel with ID '%s' not found.".formatted(channelId));
        }
        MessageChannel messageChannel = MessageUtil.getMessageChannelFromGuild(guildChannel);

        return messageChannel.retrieveMessageById(messageId).submit()
                .thenApply(Message -> new DynamicMessage(registryName, Message))
                .exceptionally(ExceptionHandler::fail);
    }
}
