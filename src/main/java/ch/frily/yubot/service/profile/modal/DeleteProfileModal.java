package ch.frily.yubot.service.profile.modal;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.profile.Profile;
import ch.frily.yubot.util.Util;
import lombok.Setter;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class DeleteProfileModal extends Modal {

    private Profile profile;

    /**
     * Sets the profile to delete
     * @param profile the profile to delete
     * @return this modal for chaining
     */
    public DeleteProfileModal setProfile(Profile profile) {
        this.profile = profile;
        this.addArgument("p", profile.profileId());
        return this;
    }

    @Override
    public String getId() {
        return "del-profile-modal";
    }

    @Override
    public String getTitle() {
        return "Profil löschen";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {

        TextInput.Builder inputBuilder = TextInput.create("approve-delete", TextInputStyle.SHORT);
        inputBuilder.setRequired(true);
        inputBuilder.setRequiredRange(7, 7);
        inputBuilder.setPlaceholder(Util.extractText(profile.name()));

        return List.of(
                TextDisplay.of(String.format("## %s löschen?", profile.name())),
                TextDisplay.of(String.format("Das Löschen des Profils **%s** kann nicht Rückgängig gemacht werden.", profile.name())),
                Label.of(
                        "\"Löschen\" eingeben, um fortzufahren",
                        inputBuilder.build())
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        String profileId = this.getArgument(event.getCustomId(), "p");
        String approveField = Objects.requireNonNull(event.getValue("approve-delete")).getAsString();

        if (approveField.equals("Löschen")) {
            ProfileRepository.deleteProfileById(profileId);
            event.reply("Profil wurde erfolgreich gelöscht.").setEphemeral(true).queue();
        } else {
            throw new InvalidStateException("Profillöschung fehlgeschlagen","Um zu bestätigen, musst du \"Löschen\" eingeben.");
        }
    }
}
