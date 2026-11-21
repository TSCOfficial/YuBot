package ch.frily.yubot.service.forummanager.container;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.service.forummanager.button.AcceptInterestRegistrationBtn;
import ch.frily.yubot.service.forummanager.button.DenyInterestRegistrationBtn;
import ch.frily.yubot.service.forummanager.button.EditAndAcceptInterestRegistrationBtn;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDateTime;

/**
 * This container is used to ask the moderators, if the interest topic is appropriate and to approve/deny the request.
 *
 * @author Aliz Rose Frily
 * @since v2.5.0
 */
public class RegisterInterestRequestContainer extends Container {

    public RegisterInterestRequestContainer(Member member, SpecialInterest interest) {
        addTextDisplay("## 📬 Neue Special-Interest Registrierung");
        addFormatedText("""
                %s (%s) möchte das Special Interest **%s** registrieren.
                """, member.getAsMention(), member.getEffectiveName(), interest.getName());
        addFormatedText("""
                ### Beschreibung
                > %s
                """, interest.getDescription());

        addFormatedText("-# <t:%d:s> (<t:%d:R>)", Util.toEpochSeconds(LocalDateTime.now()), Util.toEpochSeconds(LocalDateTime.now()));

        addLineSeparator(Separator.Spacing.LARGE);
        addTextDisplay("-# Lehne die Registrierung ab, wenn das Thema kritisch ist oder gegen die Serverregeln verstösst.");

        AcceptInterestRegistrationBtn acceptBtn = new AcceptInterestRegistrationBtn();
        acceptBtn.addArgument("interestId", interest.getId());
        EditAndAcceptInterestRegistrationBtn editBtn = new EditAndAcceptInterestRegistrationBtn();
        editBtn.addArgument("interestId", interest.getId());
        DenyInterestRegistrationBtn denyBtn = new DenyInterestRegistrationBtn();
        denyBtn.addArgument("interestId", interest.getId());

        addComponent(ActionRow.of(
                acceptBtn.build(), editBtn.build(), denyBtn.build()
        ));
    }
}
