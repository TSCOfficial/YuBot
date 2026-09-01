package ch.frily.yubot.interaction.button.btn.activemod;

import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.feature.activemod.ActiveMod;
import ch.frily.yubot.feature.activemod.Closure;
import ch.frily.yubot.database.repository.ActiveModRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
        Member member = event.getMember() == null ? Util.getMemberByUser(event.getUser()) : event.getMember();

        if (forActiveMod == null) {
            int delay = 30;
            event.editMessage(String.format("""
                   Diese Anfrage ist ungültig und hat keine Auswirkungen mehr.
                   -# Du kannst sie ruhig ignorieren
                   -# *<:timer:1522290651742339122> Nachricht wird <t:%d:R> gelöscht.*
                   """, Util.toEpochSeconds(LocalDateTime.now().plusSeconds(delay)))).setReplace(true).queue();
            event.getMessage().delete().queueAfter(delay, TimeUnit.SECONDS);
            return;
        }
        if (member.equals(forActiveMod.member())) {
            Closure.handleModActivity(member);
            event.reply("✅ Vielen dank, deine Aktivität wurde erfolgreich bestätigt.").setEphemeral(true).queue();
        } else {
            throw new PermissionDeniedException(String.format("Nur %s kann seine/ihre Anfrage bestätigen.", forActiveMod.member().getAsMention()));
        }

    }
}
