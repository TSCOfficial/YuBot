package ch.frily.yubot.service.forummanager.modal;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.exception.LimitReachedException;
import ch.frily.yubot.exception.NotFoundException;
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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
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
        return "Special Interest einsenden";
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

            try {
                SpecialInterest existingInterest = SpecialInterestRepository.getInterestByName(nameMapping.getAsString());
                event.getHook().editOriginalFormat("❌ Das Special-Interest \"%s\" kann nicht eingereicht werden, da es bereits existiert. Siehe %s.", existingInterest.getName(), existingInterest.getPost().getJumpUrl()).queue();
                return;
            } catch (NotFoundException _) {}


            SpecialInterest submittedInterest = new SpecialInterest();
            submittedInterest.setName(nameMapping.getAsString());
            submittedInterest.setDescription(descriptionMapping.getAsString());
            submittedInterest.setOriginalPoster(event.getUser());
            submittedInterest.setNotifyOP(notifyOPMapping.getAsBoolean());

            beta_SubmissionFuzzySearchDebug(submittedInterest, similarInterests, SpecialInterestRepository.getInterests());

            if (similarInterests.isEmpty()) {
                int interestId = SpecialInterestRepository.createInterest(submittedInterest);
                submittedInterest.setId(interestId);

                InterestForumManager.requestInterestApproval(event.getMember(), submittedInterest, () -> {
                    String sendDmNotificationMsg = submittedInterest.isNotifyOP() ? "Du wirst über Änderungen des Status deiner Einsendung per DM benachrichtigt." : "";
                    event.getHook().editOriginal("""
                            ✅ **Die Anfrage wurde gesendet.**
                            Das Moderationsteam wird es sich baldmöglichst anschauen — dies kann jedoch etwas Zeit in anspruch nehmen.
                            %s
                            """.formatted(sendDmNotificationMsg)).queue();
                });

            } else {
                int randomId = Math.toIntExact(Math.round(Math.random() * 100));
                submittedInterest.setId(randomId); // generate random pseudo id for session storage. This id will get overwritten when later saved to the database
                SessionStorageService.getInstance().addStorage("special-interest-registration-" + submittedInterest.getId(), event.getMember(), submittedInterest, 15);

                AskSimilarInterestContainer similarInterestContainer = new AskSimilarInterestContainer(submittedInterest, similarInterests);
                event.getHook().editOriginalComponents(similarInterestContainer.build()).useComponentsV2().queue();
            }

        } else {
            throw new LimitReachedException("Du kannst nicht mehr als 2 Interests gleichzeitig einsenden.", "Bitte warte, bis das Modteam deine Einsendungen bearbeitet hat, bevor du nochmals ein Interest einsendest.");
        }

    }

    private void beta_SubmissionFuzzySearchDebug(SpecialInterest submittedInterest, List<SpecialInterest> similarInterests, List<SpecialInterest> allInterests) {
        MessageCreateBuilder msgBuilder = new MessageCreateBuilder();
        msgBuilder.useComponentsV2();
        msgBuilder.addComponents(TextDisplay.of("# Special-Interest - Fuzzy Search Debug View"));

        msgBuilder.addComponents(TextDisplay.of("**Submitted interest:**"));
        msgBuilder.addComponents(TextDisplay.of(submittedInterest.getName()));

        msgBuilder.addComponents(TextDisplay.ofFormat("**Found similar interest (%d):**", similarInterests.size()));
        if (!similarInterests.isEmpty()) {
            msgBuilder.addComponents(TextDisplay.of(String.join("\n", similarInterests.stream().map(SpecialInterest::getName).toList())));
        }

        msgBuilder.addComponents(TextDisplay.ofFormat("**All registered interest (%d):**", allInterests.size()));
        if (!allInterests.isEmpty()) {
            msgBuilder.addComponents(TextDisplay.of(String.join("\n", allInterests.stream().map(SpecialInterest::getName).toList())));
        }

        EnvKey.CHANNEL_SERVERCONFIG.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER).sendMessage(msgBuilder.build()).queue();
    }
}
