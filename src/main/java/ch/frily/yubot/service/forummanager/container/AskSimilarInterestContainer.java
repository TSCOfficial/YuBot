package ch.frily.yubot.service.forummanager.container;


import ch.frily.yubot.container.Container;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.service.forummanager.button.RejectSimilarInterestsBtn;
import ch.frily.yubot.service.forummanager.button.SelectSimilarInterestBtn;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This container is used to ask the user, at the registration, if the found similar interests are the one the user wants to register.
 *
 * @author Aliz Rose Frily
 * @since v2.5.0
 */
public class AskSimilarInterestContainer extends Container {
    /**
     *
     * @param registeringInterest the interest that the user wants to register
     * @param similarInterests existing interests that are similar to registeringInterest
     */
    public AskSimilarInterestContainer(SpecialInterest registeringInterest, List<SpecialInterest> similarInterests) {
        Objects.requireNonNull(similarInterests);
        if (similarInterests.isEmpty()) {
                throw new InvalidStateException("Liste der ähnlichen Interests darf nicht leer sein.");
        }

        if (similarInterests.size() == 1) {
            addFormatedText("## Meintest du \"%s\"?", similarInterests.getFirst().getName());
            addFormatedText("Es existiert ein potenziell ähnliches Interesse. Bitte wähle aus, ob dieses existierende Interesse zu deinem Thema passt. Wenn nicht, kannst du dein Interesse mit dem darunterstehenden Button registrieren.", similarInterests.getFirst().getPost().getJumpUrl());
            similarInterests.forEach(this::addSimilarInterestSection);
        } else {
            addFormatedText("## Meintest du \"%s\",... ?", similarInterests.getFirst().getName());
            addTextDisplay("Es existieren %d potenzielle ähnliche Interessen. Bitte wähle aus, ob eins dieser existierenden Interessen zu deinem Thema passt. Wenn nicht, kannst du dein Interesse mit dem darunterstehenden Button registrieren.");
            similarInterests.forEach(this::addSimilarInterestSection);
        }

        RejectSimilarInterestsBtn rejectSuggesionsBtn = new RejectSimilarInterestsBtn();
        rejectSuggesionsBtn.addArgument("interestId", registeringInterest.getId());
        addComponent(ActionRow.of(
            rejectSuggesionsBtn.build()
        ));
    }

    private void addSimilarInterestSection(SpecialInterest similarInterest) {
        SelectSimilarInterestBtn selectInterestBtn = new SelectSimilarInterestBtn();
        selectInterestBtn.addArgument("interestId", similarInterest.getId()); // uses local pseudo id to find interest from session storage
        addSection(
                selectInterestBtn.build(),
                TextDisplay.ofFormat("%s (%s)", similarInterest.getName(), similarInterest.getPost().getJumpUrl())
        );
    }
}
