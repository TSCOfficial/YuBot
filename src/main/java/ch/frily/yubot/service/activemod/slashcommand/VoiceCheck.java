package ch.frily.yubot.service.activemod.slashcommand;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class VoiceCheck implements ISlashCommand {
    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Vermerkte den Sprechkanal in #\uD83D\uDCCB┃support-voice-check als geprüft";
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_SUPPORT,
                EnvKey.ROLE_MODERATOR
        ).map(EnvKey::getAsRole).toList();
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        if (event.getChannelType() == ChannelType.VOICE) {
            TextChannel checkChannel = EnvKey.CHANNEL_VOICECHECK.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER);
            checkChannel.sendMessage(String.format("%s (%s) ist in Ordnung", event.getChannel().getAsMention(), event.getChannel().getName())).queue(_ -> {
                event.getHook().editOriginal(String.format("✅ %s wurde als geprüft vermerkt", event.getChannel().getAsMention())).queue();
            });
        } else {
            throw new InvalidStateException(String.format("%s ist kein Sprechkanal.", event.getChannel().getAsMention()), "Nur Sprechkanäle können als geprüft vermerkt werden.");
        }
    }
}
