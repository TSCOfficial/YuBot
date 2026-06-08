package ch.frily.yubot.feature;

import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Slf4j
public class TeamlistCmd implements ISlashCommand {

    @Override
    public String getName() {
        return "teamlist";
    }

    @Override
    public String getDescription() {
        return "Sende oder Aktualisiere die Teamliste.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public Map<String, List<?>> getAutocomplete() {
        return Map.of();
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return List.of(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        MessageEmbed embed = Teamlist.getInstance().generateEmbed();
        TextChannel channel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_DASTEAM);

        event.deferReply(true).queue();
        EnvResolver.getMessageById(event.getGuild().getIdLong(), channel.getIdLong(), channel.getLatestMessageIdLong()).thenAccept(message -> {
            message.editMessageEmbeds(embed).queue();
        }).exceptionally(error -> {
            channel.sendMessage("").addEmbeds(embed).queue();
            return null;
        });
        event.getHook().sendMessage("✅ Teamliste wurde aktualisiert.").setEphemeral(true).queue();



    }
}
