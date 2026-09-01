package ch.frily.yubot.interaction.command.cmd.send;

import ch.frily.yubot.embed.StaticEmbedRegistry;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.dynamicmsg.DynamicMessageList;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SendEmbedCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "embed";
    }

    @Override
    public String getDescription() {
        return "Sende eine Einbettung";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "embed", "Wähle eine Einbettung aus", true)
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

        event.getChannel()
                .sendMessageEmbeds(embed)
                .queue(
                        ThrowingConsumer.wrap(event, message -> {
                            DynamicMessageList dynamicMessage = DynamicMessageList.fromRegistryName(embedValue);

                            if (dynamicMessage != null) {
                                dynamicMessage.remember(message);
                            }

                            event.reply("✅ Einbettung \"" + humanizeEnumName(embedValue) + "\" erfolgreich gesendet.")
                                    .setEphemeral(true)
                                    .queue();
                        })
                );
    }

    private String humanizeEnumName(String enumValue) {
        return enumValue.toLowerCase().replace("_", "-");
    }
}
