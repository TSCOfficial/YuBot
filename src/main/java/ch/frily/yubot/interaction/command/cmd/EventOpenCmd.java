package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.EventControl;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class EventOpenCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "open";
    }

    @Override
    public String getDescription() {
        return "Öffne den Event channel";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        if (EventControl.getInstance().isPermitted("*", event.getMember())) {
            event.reply("Du bist berechtigt").setEphemeral(true).queue();
        } else {
            event.reply("Du bist nicht berechtigt").setEphemeral(true).queue();
        }
    }
}
