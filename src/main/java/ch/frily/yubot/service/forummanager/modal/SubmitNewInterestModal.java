package ch.frily.yubot.service.forummanager.modal;

import ch.frily.yubot.exception.LimitReachedException;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.forummanager.InterestForumManager;
import ch.frily.yubot.service.forummanager.container.AskSimilarInterestContainer;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import ch.frily.yubot.service.storage.SessionStorageService;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.checkbox.Checkbox;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Modal to request all infos for the new interest
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
@Slf4j
public class SubmitNewInterestModal extends Modal {
    @Override
    public String getId() {
        return "request-interest-modal";
    }

    @Override
    public String getTitle() {
        return "Neues Interesse registrieren";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() throws SQLException {
        TextInput.Builder interestName = TextInput.create("interest-name", TextInputStyle.SHORT);
        interestName.setRequiredRange(4, ThreadChannel.MAX_NAME_LENGTH);
        interestName.setRequired(true);
        interestName.setPlaceholder("Name eingeben...");

        TextInput.Builder interestDescription = TextInput.create("interest-description", TextInputStyle.PARAGRAPH);
        interestDescription.setRequiredRange(25, 400);
        interestDescription.setRequired(true);
        interestDescription.setPlaceholder("Beschreibung eingeben...");

        Checkbox notifyOP = Checkbox.of("notify-op", true);

        return List.of(
                TextDisplay.of("""
        Um ein neues Interest registrieren zu können, muss ein Namen und eine Beschreibung des Themas angegeben werden.
        -# Das Serverteam wird die Anfrage anschliessend überprüfen - dies kann etwas dauern.
        """),
                Label.of(
                        "Name",
                        "Dies wird, bei der Freigabe, als Post-Titel verwendet",
                        interestName.build()
                ),
                Label.of(
                        "Beschreibung",
                        "Dies wird, bei der Freigabe, als Post-Beschreibung (\"first message\") verwendet.",
                        interestDescription.build()
                ),
                Label.of(
                        "Über DM benachricht werden",
                        "Werde benachrichtigt, wenn dein Interest abgelehnt bzw. bearbeitet wurde.",
                        notifyOP
                )
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        event.deferReply(true).queue();

        int pendingInterestCount = SpecialInterestRepository.getInterestsByUser(event.getUser()).stream().filter(interest -> {
            return interest.getRequestStatus() == SpecialInterest.RequestStatus.PENDING;
        }).toList().size();
        if (pendingInterestCount < SpecialInterest.MAX_SIMULTANEOUS_PENDING_INTERESTS) {
            ModalMapping nameMapping = event.getValue("interest-name");
            ModalMapping descriptionMapping = event.getValue("interest-description");
            ModalMapping notifyOPMapping = event.getValue("notify-op");

            List<SpecialInterest> similarInterests = InterestForumManager.getSimilarInterests(nameMapping.getAsString());


            SpecialInterest registeredInterest = new SpecialInterest();
            registeredInterest.setName(nameMapping.getAsString());
            registeredInterest.setDescription(descriptionMapping.getAsString());
            registeredInterest.setOriginalPoster(event.getUser());
            registeredInterest.setNotifyOP(notifyOPMapping.getAsBoolean());

            if (similarInterests.isEmpty()) {
                int interestId = SpecialInterestRepository.createInterest(registeredInterest);
                registeredInterest.setId(interestId);

                InterestForumManager.requestInterestApproval(event.getMember(), registeredInterest, () -> {
                    String sendDmNotificationMsg = registeredInterest.isNotifyOP() ? "Du wirst über Änderungen des Status deiner Einsendung per DM benachrichtigt." : "";
                    event.getHook().editOriginal("""
                            ✅ **Die Anfrage wurde gesendet.**
                            Das Moderationsteam wird es sich baldmöglichst anschauen — dies kann jedoch etwas Zeit in anspruch nehmen.
                            %s
                            """.formatted(sendDmNotificationMsg)).queue();
                });

            } else {
                int randomId = Math.toIntExact(Math.round(Math.random() * 100));
                registeredInterest.setId(randomId); // generate random pseudo id for session storage. This id will get overwritten when later saved to the database
                SessionStorageService.getInstance().addStorage("special-interest-registration-" + registeredInterest.getId(), event.getMember(), registeredInterest, 15);

                AskSimilarInterestContainer similarInterestContainer = new AskSimilarInterestContainer(registeredInterest, similarInterests);
                event.getHook().editOriginalComponents(similarInterestContainer.build()).useComponentsV2().queue();
            }


        } else {
            throw new LimitReachedException("Du kannst nicht mehr als 2 Interests gleichzeitig in bearbeitung haben.");
        }

    }
}
