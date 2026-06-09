package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.embed.StaticEmbedRegistry;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SendEmbedCmd implements ISlashCommand {
    @Override
    public String getName() {
        return "send";
    }

    @Override
    public String getDescription() {
        return "Sende eine Einbettung";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "embed", "Wähle eine Einbettung aus", false)
                        .addChoices(Arrays.stream(StaticEmbedRegistry.values()).map(embed -> {
                            return new Command.Choice(humanizeEnumName(embed.name()), embed.name());
                        }).toList()
                        )
        );
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        String embedValue = event.getOption("embed").getAsString();
        MessageEmbed embed = StaticEmbedRegistry.valueOf(embedValue).getEmbed();
        event.getChannel().sendMessageEmbeds(embed).queue();
        event.reply("✅ Einbettung \"" + humanizeEnumName(embedValue) + "\" erfolgreich gesendet.").setEphemeral(true).queue();
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashCommand.super.getDefaultPermissions();
    }

    private String humanizeEnumName(String enumValue) {
        return enumValue.toLowerCase().replace("_", "-");
    }
}
