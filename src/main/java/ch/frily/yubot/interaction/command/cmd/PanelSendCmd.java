package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.Client;
import ch.frily.yubot.embed.PanelEmbed;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.interaction.button.btn.AwarenessButton;
import ch.frily.yubot.interaction.button.btn.SupportButton;
import ch.frily.yubot.util.EnvKey;
import javassist.NotFoundException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class PanelSendCmd implements ISlashSubcommand {

    @Override
    public String getName() {
        return "panel";
    }

    @Override
    public String getDescription() {
        return "Sende panel";
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
        return List.of();
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        try {
            MessageEmbed embed = new PanelEmbed().build();
            TextChannel channel = event.getGuild().getTextChannelById(Client.getInstance().getConfig().get(EnvKey.CHANNEL_TICKET.name()));

            if (channel == null) {
                throw new NotFoundException("Channel not found");
            }
            ActionRow actionrow = ActionRow.of(
                    SupportButton.getInstance().build(),
                    AwarenessButton.getInstance().build()
            );
            channel.sendMessageEmbeds(embed).setComponents(actionrow).queue();
            event.reply("✅ Panel erfolgreich gesendet.").setEphemeral(true).queue();

        } catch (NotFoundException notFoundException) {
            event.reply(notFoundException.getMessage()).setEphemeral(true).queue();
        }
    }
}
