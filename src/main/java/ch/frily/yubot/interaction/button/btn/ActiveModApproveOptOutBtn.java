package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

/**
 * When the last active mod wants to opt-out via command, the bot askes to approve the opt-out before closing the server
 */
public class ActiveModApproveOptOutBtn extends Button {
    @Override
    public String getId() {
        return "approve-activemod-optout";
    }

    @Override
    public String getLabel() {
        return "Bestätige";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        Role activeMod = EnvResolver.getRoleById(1513639704870912130L);
        event.getGuild().removeRoleFromMember(event.getMember(), activeMod).submit().thenAccept(_ -> {

            event.reply("Opt-out erfolgreich.\n-# Der Server wird nun geschlossen.").setEphemeral(true).queue();
        });
    }
}
