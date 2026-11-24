package ch.frily.yubot.service.forummanager.container;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.service.forummanager.InterestForumManager;
import ch.frily.yubot.service.forummanager.button.AcceptInterestSubmissionBtn;
import ch.frily.yubot.service.forummanager.button.DenyInterestSubmissionBtn;
import ch.frily.yubot.service.forummanager.button.EditAndAcceptInterestSubmissionBtn;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.entities.Member;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This container is used to ask the moderators, if the interest topic is appropriate and to approve/deny the request.
 *
 * @author Aliz Rose Frily
 * @since v2.5.0
 */
public class NewInterestSubmittedContainer extends Container {

    public NewInterestSubmittedContainer(Member member, SpecialInterest interest) {
        addTextDisplay("## 📬 Neues Special-Interest eingesendet");
        addFormatedText("""
                %s (%s) hat das Interest **%s** eingesendet.
                """, member.getAsMention(), member.getEffectiveName(), interest.getName());
        addFormatedText("""
                ### Beschreibung
                "%s"
                """, interest.getDescription());


        try {
            List<SpecialInterest> similarInterests = InterestForumManager.getSimilarInterests(interest.getName(), 0.5);
            if (!similarInterests.isEmpty()) {
                addTextDisplay("### Potenziell ähnliche Special-Interests");
                for (SpecialInterest similarInterest : similarInterests) {
                    addFormatedText("""
                    - "%s" (%s)
                    """, similarInterest.getName(), similarInterest.getPost().getJumpUrl());
                }
            }
        } catch (SQLException _) {}

        addFormatedText("-# <t:%d:s> (<t:%d:R>)", Util.toEpochSeconds(LocalDateTime.now()), Util.toEpochSeconds(LocalDateTime.now()));

        addLineSeparator(Separator.Spacing.LARGE);
        addTextDisplay("""
                -# Bearbeite die Einsendung, wenn es Tippfehler oder weitere kleinigkeiten gibt.
                -# Lehne die Einsendung ab, wenn das Thema bereits existiert, oder gegen die Serverregeln verstösst.
                """);

        AcceptInterestSubmissionBtn acceptBtn = new AcceptInterestSubmissionBtn();
        acceptBtn.addArgument("interestId", interest.getId());
        EditAndAcceptInterestSubmissionBtn editBtn = new EditAndAcceptInterestSubmissionBtn();
        editBtn.addArgument("interestId", interest.getId());
        DenyInterestSubmissionBtn denyBtn = new DenyInterestSubmissionBtn();
        denyBtn.addArgument("interestId", interest.getId());

        addComponent(ActionRow.of(
                acceptBtn.build(), editBtn.build(), denyBtn.build()
        ));
    }
}
