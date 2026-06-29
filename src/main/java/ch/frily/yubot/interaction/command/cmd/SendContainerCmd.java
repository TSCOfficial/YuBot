package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.embed.StaticEmbedRegistry;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.DynamicMessageList;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
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

public class SendContainerCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "container";
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
        String containerValue = event.getOption("container").getAsString();
        List<Container> containers = StaticContainerRegistry.valueOf(containerValue).getContainers();

        event.getChannel()
                .sendMessageComponents(containers).useComponentsV2()
                .setAllowedMentions(List.of()).queue(ThrowingConsumer.wrap(event, message -> {
                    DynamicMessageList dynamicMessage = DynamicMessageList.fromRegistryName(containerValue);

                    if (dynamicMessage != null) {
                        dynamicMessage.remember(message);
                    }

                    event.reply("✅ Container \"" + humanizeEnumName(containerValue) + "\" erfolgreich gesendet.")
                            .setEphemeral(true)
                            .queue();
                }));
    }

    private String humanizeEnumName(String enumValue) {
        return enumValue.toLowerCase().replace("_", "-");
    }
}
