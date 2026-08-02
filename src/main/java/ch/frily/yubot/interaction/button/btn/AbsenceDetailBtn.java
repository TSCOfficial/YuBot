package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.feature.Absence;
import ch.frily.yubot.feature.AbsenceRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Allowes the team to view an absance in detail
 */
@Slf4j
public class AbsenceDetailBtn extends Button {

    @Override
    public String defineId() {
        return "absence-detail-btn";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public EmojiUnion getEmoji() {
        return Emoji.fromFormatted("<:eye:1532747011730702336>");
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        log.info(event.getComponentId());
        log.info(getArgument(event, "absence_id"));
        Absence absence = AbsenceRepository.getAbsenceById(Integer.parseInt(getArgument(event, "absence_id")));
        event.reply(absence.toString()).queue();
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_YUTEAM
        ).map(EnvResolver::getRoleById).toList();
    }
}
