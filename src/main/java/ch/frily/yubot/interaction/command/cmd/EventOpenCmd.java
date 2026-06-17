package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.EventControl;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
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

        EventControl.getInstance().isPermittedElseThrow("*", event.getMember());

        EventControl.getInstance().toggleChannelPermissions(true);
        event.reply("✅ Eventkanal erfolgreich geöffnet.").setEphemeral(true).queue();

    }
}
