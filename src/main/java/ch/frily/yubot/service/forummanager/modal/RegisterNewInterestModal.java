package ch.frily.yubot.service.forummanager.modal;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.exception.LimitReachedException;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.forummanager.container.RegisterInterestRequestContainer;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Modal to request all infos for the new interest
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
@Slf4j
public class RegisterNewInterestModal extends Modal {
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

            List<SpecialInterest> allInterests = SpecialInterestRepository.getInterests();
            Map<String, SpecialInterest> terms = allInterests.stream().map(interest -> {
                return Map.of(interest.getName(), interest);
            });
            List<String> similarTerms = Util.fuzzySearch(nameMapping.getAsString(), terms);


            SpecialInterest interest = new SpecialInterest();
            interest.setName(nameMapping.getAsString());
            interest.setDescription(descriptionMapping.getAsString());
            interest.setOriginalPoster(event.getUser());

            int interestId = SpecialInterestRepository.createInterest(interest);
            interest.setId(interestId);

            if (similarTerms.isEmpty()) {
                // send request to moderators
                RegisterInterestRequestContainer requestContainer = new RegisterInterestRequestContainer(event.getMember(), interest);
                EnvKey.CHANNEL_SERVERCONFIG.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER)
                        .sendMessageComponents(requestContainer.build()).useComponentsV2().setAllowedMentions(List.of()).queue(_ -> {
                            event.getHook().editOriginal("✅ Die Anfrage wurde gesendet.").queue();
                        });
            } else {
                StringBuilder sb = new StringBuilder();
                if (similarTerms.size() == 1) {
                    sb.append(String.format("Meintest du %s?", similarTerms.getFirst()))
                    sb.append("Es existiert ein anderes Interest, welches potenziell dein Thema sein könnte:");
                } else {
                    sb.append(String.format("Es existieren %d andere Interests, welche potenziell dein Thema"))
                }


                event.getHook().editOriginal(String.join("\n", similarTerms)).queue();
            }


        } else {
            throw new LimitReachedException("Du kannst nicht mehr als 2 Interests gleichzeitig in bearbeitung haben.");
        }

    }
}
