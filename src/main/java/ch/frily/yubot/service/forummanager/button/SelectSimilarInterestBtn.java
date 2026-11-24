package ch.frily.yubot.service.forummanager.button;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.forummanager.InterestForumManager;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import ch.frily.yubot.service.storage.SessionStorageService;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Used to select a specific similar interest, when the user wants to register a new one.
 *
 * @author Aliz Rose Frily
 * @since v2.5.0
 */
public class SelectSimilarInterestBtn extends Button {
    @Override
    public String getId() {
        return "similar-interest-tn";
    }

    @Override
    public String getLabel() {
        return "Auswählen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException {
        String similarInterestId = getArgument(event.getComponentId(), "interestId");
        SpecialInterest similarInterest = SpecialInterestRepository.getInterestById(Integer.parseInt(similarInterestId));
        Objects.requireNonNull(similarInterest);

        event.replyFormat("Die registrierung deines interests wurde abgebrochen. Besuche %s um das ähnliche Thema zu sehen.", similarInterest.getPost().getJumpUrl()).setEphemeral(true).queue();
        event.getMessage().delete().queue();
    }
}
