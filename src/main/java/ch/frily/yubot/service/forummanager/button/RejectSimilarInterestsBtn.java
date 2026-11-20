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

/**
 * Used to reject all similar interest suggestions and register own new interest
 *
 * @author Aliz Rose Frily
 * @since v2.5.0
 */
public class RejectSimilarInterestsBtn extends Button {
    @Override
    public String getId() {
        return "reject-interest-sugg-btn";
    }

    @Override
    public String getLabel() {
        return "Nein, keiner dieser Vorschläge passt";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException {
        event.deferReply(true).queue();
        String localInterestId = getArgument(event.getComponentId(), "interestId");
        SpecialInterest interest = SessionStorageService.getInstance().getStorage("special-interest-registration-" + localInterestId, event.getMember(), SpecialInterest.class);

        int interestId = SpecialInterestRepository.createInterest(interest);
        interest.setId(interestId); // overwrite id with the correct one from the DB

        InterestForumManager.requestInterestApproval(event.getMember(), interest, () -> {
            event.getHook().editOriginal("✅ Die Anfrage wurde gesendet. Das Moderationsteam wird es sich baldmöglichst anschauen — dies kann etwas Zeit in anspruch nehmen.").queue();
            event.getMessage().delete().queue();
        });
    }
}
