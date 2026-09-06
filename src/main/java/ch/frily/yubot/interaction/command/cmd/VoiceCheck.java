package ch.frily.yubot.interaction.command.cmd;

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

import java.sql.SQLException;
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
        ).map(EnvResolver::getRoleById).toList();
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        event.deferReply(true).queue();
        if (event.getChannelType() == ChannelType.VOICE) {
            TextChannel checkChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_VOICECHECK);
            checkChannel.sendMessage(String.format("%s ist in Ordnung", event.getChannel().getAsMention())).queue(_ -> {
                event.getHook().editOriginal(String.format("✅ %s wurde als geprüft vermerkt", event.getChannel().getAsMention())).queue();
            });
        } else {
            throw new InvalidStateException(String.format("%s ist kein Sprechkanal.", event.getChannel().getAsMention()), "Nur Sprechkanäle können als geprüft vermerkt werden.");
        }
    }
}
