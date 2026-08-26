package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.container.ActiveModStatisticContainer;
import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.feature.ActiveModTrackingRepository;
import ch.frily.yubot.interaction.button.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ShowStatisticBtn extends Button {
    @Override
    public String getId() {
        return "show-statistic-btn";
    }

    @Override
    public String getLabel() {
        return "Statistik anzeigen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public EmojiUnion getEmoji() {
        return Emoji.fromFormatted("📊");
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        event.deferReply(true).queue();
        Map<Member, List<ActiveModTracking>> activeModTrackings = ActiveModTrackingRepository.getActiveModTrackingsAsMap();
        activeModTrackings = ActiveModTrackingRepository.completeWithMissingModerators(activeModTrackings);
        ActiveModStatisticContainer activeModStatisticContainer = new ActiveModStatisticContainer(activeModTrackings, event.getMember());
        event.getHook().sendMessageComponents(activeModStatisticContainer.build()).useComponentsV2().setAllowedMentions(List.of()).setEphemeral(true).queue();
    }

    @Override
    public List<Role> getAllowedRoles() {
        return super.getAllowedRoles();
    }
}
