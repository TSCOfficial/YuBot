package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.DynamicMessageList;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class SendDynamicMsgCmd implements ISlashSubcommand { // todo replace by an upsert with argument-support
    @Override
    public String getName() {
        return "dynamic";
    }

    @Override
    public String getDescription() {
        return "Sende eine dynamische Nachricht";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "message", "Wähle eine dynamische Nachricht aus", true)
                        .addChoices(Arrays.stream(DynamicMessageList.values()).map(container -> {
                            return new Command.Choice(humanizeEnumName(container.name()), container.name());
                        }).toList()
                        )
        );
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        String containerValue = event.getOption("message").getAsString();
        DynamicMessageList.valueOf(containerValue).update(); // this can cause errors when the dynamic messages requests arguments!

        event.reply("✅ Dynamische Nachricht \"" + humanizeEnumName(containerValue) + "\" erfolgreich gesendet.")
                .setEphemeral(true)
                .queue();
    }

    private String humanizeEnumName(String enumValue) {
        return enumValue.toLowerCase().replace("_", "-");
    }
}
