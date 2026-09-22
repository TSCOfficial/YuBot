package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.interaction.modal.Modal;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AddProfileModal extends Modal {

    private Profile profile;

    @Override
    public String getId() {
        return "add-profile-modal";
    }

    @Override
    public String getTitle() {
        return "Profil hinzufügen";
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        addArgument("p", profile.profileId());
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {

        TextInput.Builder profilename = TextInput.create("profilename", TextInputStyle.SHORT);
        profilename.setRequired(true);
        profilename.setRequiredRange(2, 80);
        if (profile != null && !profile.name().isBlank()) {
            profilename.setValue(profile.name());
        }

        TextInput.Builder proxy = TextInput.create("proxy", TextInputStyle.SHORT);
        proxy.setRequired(false);
        proxy.setRequiredRange(1, 6);

        if (profile != null && !profile.proxy().isBlank()) {
            proxy.setValue(profile.proxy());
        }

        TextInput.Builder profilepicture = TextInput.create("profilepicture-url", TextInputStyle.SHORT);
        profilepicture.setRequired(false);
        if (profile != null && !profile.profilePicture().isBlank()) {
            profilepicture.setValue(profile.profilePicture());
        }


        return List.of(
                TextDisplay.of("Dieses System erlaubt Personen mit Dissoziativer Identittsstörung, oder ähnliche Diagnosen, ein eigenes \"Profil\" für deren jeweilige Personen/Alters/Fronts zu kreieren."),
                Label.of(
                        "Profilname", profilename.build()
                ),
                Label.of(
                        "Proxy", "Verwende Proxy als Prefix in deinen Nachrichten um sie mit diesem Profil zu versenden.", proxy.build()
                ),
//                Label.of("Profilbild", AttachmentUpload.create("profilepicture").build())
                Label.of("Profilbild", "Erlaubte Bildformate: .jepg, .jpg, .png. Dateiupload wird nicht unterstützt.", profilepicture.build() )
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        boolean isEditMode = false;
        Profile profile = null;
        try {
            String profileId = getArgument(event.getCustomId(), "p");
            profile = ProfileRepository.getProfileById(profileId);
            isEditMode = true;
        } catch (InvalidStateException e) {
            // ignore missing argument
            log.info(e.getMessage());
        }

        String id = isEditMode ? profile.profileId() : UUID.randomUUID().toString();
        String profilename = event.getValue("profilename").getAsString().trim();
        String proxy = null;
        if (event.getValue("proxy") != null) {
            proxy = event.getValue("proxy").getAsString().trim();
        }

        String imageUrl = null;
        if (event.getValue("profilepicture-url") != null) {
            imageUrl = event.getValue("profilepicture-url").getAsString().trim();
            if (!imageUrl.isBlank() && !imageUrl.endsWith(".jpeg") && !imageUrl.endsWith(".jpg") && !imageUrl.endsWith(".png")) {
                throw new InvalidStateException("Profilbild konnte nicht definiert werden.", "Dateiformat muss `.jpeg`, `.jpg` oder `.png` sein.");
            }
        }

        Profile newProfile = new Profile(id, event.getMember(), profilename, false, imageUrl, proxy);
        if (isEditMode) {
            ProfileRepository.updateProfile(newProfile);
        } else {
            ProfileRepository.createProfile(newProfile);
        }
        event.reply(String.format("✅ Das Profil \"%s\" wurde erfolgreich %s.\n-# Wende es mit </profile show:1542519831729934447> an.",  profilename, isEditMode ? "aktualisiert" : "erstellt")).setEphemeral(true).queue();
    }
}
