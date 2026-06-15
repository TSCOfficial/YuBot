package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.embed.StaticEmbedRegistry;
import ch.frily.yubot.interaction.command.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SendContainerCmd implements ISlashCommand {
    @Override
    public String getName() {
        return "sendcontainer";
    }

    @Override
    public String getDescription() {
        return "Sende ein container";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "container", "Wähle ein container aus", true)
                        .addChoices(Arrays.stream(StaticContainerRegistry.values()).map(container -> {
                            return new Command.Choice(humanizeEnumName(container.name()), container.name());
                        }).toList()
                        )
        );
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        String embedValue = event.getOption("container").getAsString();
        List<Container> containers = StaticContainerRegistry.valueOf(embedValue).getContainers();

        containers.forEach(container -> {
            event.getChannel().sendMessageComponents(container).useComponentsV2().queue();
        });

        event.reply("✅ Container \"" + humanizeEnumName(embedValue) + "\" erfolgreich gesendet.").setEphemeral(true).queue();
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashCommand.super.getDefaultPermissions();
    }

    private String humanizeEnumName(String enumValue) {
        return enumValue.toLowerCase().replace("_", "-");
    }
}
