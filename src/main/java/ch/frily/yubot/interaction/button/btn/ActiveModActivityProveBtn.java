package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.feature.ActiveMod;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.feature.ActiveModRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.button.IButton;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Accept/Prove the activity of an active-mod when the moderator was inactive for a while
 */
public class ActiveModActivityProveBtn extends Button {
    @Override
    public String getId() {
        return "activemod-activity-prove-btn";
    }

    @Override
    public String getLabel() {
        return "Bestätigen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SUCCESS;
    }

    @Override
    public void execute(@NotNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException {
        ActiveMod forActiveMod = ActiveModRepository.getModeratorByActivityRequestMessageId(event.getMessageIdLong());
        if (event.getMember().equals(forActiveMod.member())) {
            Closure.handleModActivity(event.getMember());
            event.reply("✅ Vielen dank, deine Aktivität wurde erfolgreich bestätigt.").setEphemeral(true).queue();
        } else {
            throw new PermissionDeniedException(String.format("Nur %s kann seine/ihre Anfrage bestätigen.", forActiveMod.member().getAsMention()));
        }

    }
}
