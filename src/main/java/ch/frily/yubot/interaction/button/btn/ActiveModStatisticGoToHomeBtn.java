package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.container.ActiveModStatisticContainer;
import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.feature.ActiveModTrackingRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.button.IButton;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ActiveModStatisticGoToHomeBtn extends Button {
    @Override
    public String getId() {
        return "activemod-statistic-home-btn";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public EmojiUnion getEmoji() {
        return Emoji.fromFormatted("<:home:1526737131282763816>");
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException {
        Map<Member, List<ActiveModTracking>> activeModTrackings = ActiveModTrackingRepository.getActiveModTrackingsAsMap();
        activeModTrackings = ActiveModTrackingRepository.completeWithMissingModerators(activeModTrackings);
        ActiveModStatisticContainer activeModStatisticContainer = new ActiveModStatisticContainer(activeModTrackings, event.getMember());
        event.editComponents(activeModStatisticContainer.build()).useComponentsV2().setAllowedMentions(List.of()).queue();
    }
}
