package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.button.IButton;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This message always contains exactly one member-mention of the concerning ex-activemod.
 * This mention inside the message (event.getMessage() is used to identify the ex-activemod and only allow them to execute this button.
 */
@Slf4j
public class DeleteActivityRequestMsgBtn extends Button {
    @Override
    public String getId() {
        return "delete-activity-request-msg-btn";
    }

    @Override
    public String getLabel() {
        return "Nachricht Löschen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.DANGER;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException {
        Pattern pattern = Pattern.compile("<@\\d+>");
        Matcher matcher = pattern.matcher(event.getMessage().getContentRaw());
        if (matcher.find()) {
            String memberId = matcher.group().replace("<@", "").replace(">", "");
            event.getGuild().retrieveMemberById(memberId).queue(member -> {
                if (member.getId().equals(event.getMember().getId())) {
                    event.getMessage().delete().queue();
                    event.reply("Nachricht gelöscht.").setEphemeral(true).queue();
                } else {
                    throw new PermissionDeniedException("Nur der/die betreffende Moderator*in kann deren Nachricht löschen.");
                }
            });
        } else {
            throw new InvalidStateException("Es konnte keine Member-ID gefunden werden.");
        }
    }
}
