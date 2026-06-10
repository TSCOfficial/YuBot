package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.EventControl;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;

public class EventCloseCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "close";
    }

    @Override
    public String getDescription() {
        return "Schliesse den Event channel";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        try {
            EventControl.getInstance().isPermittedElseThrow("*", event.getMember());
            
            EventControl.getInstance().toggleChannelPermissions(false);
            event.reply("✅ Eventkanal erfolgreich geöffnet.").setEphemeral(true).queue();
        } catch (PermissionException permissionException) {
            event.reply("❌ Du bist nicht berechtigt dies auszuführen!\n-# " + permissionException.getMessage()).setEphemeral(true).queue();
        }
    }
}
