package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.interaction.modal.Modal;
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
import java.util.List;
import java.util.UUID;

@Slf4j
public class AddProfileModal extends Modal {
    @Override
    public String getId() {
        return "add-profile-modal";
    }

    @Override
    public String getTitle() {
        return "Profil hinzufügen";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {
        TextInput.Builder profilename = TextInput.create("profilename", TextInputStyle.SHORT);
        profilename.setRequired(true);
        profilename.setRequiredRange(2, 80);


        return List.of(
                TextDisplay.of("Dieses System erlaubt Personen mit Dissoziativer Identittsstörung, oder Ähnliche Diagnosen, ein eigenes \"Profil\" für deren jeweilige Alters/Fronts/Personen zu kreieren."),
                Label.of(
                        "Profilname", profilename.build()
                ),
//                Label.of("Profilbild", AttachmentUpload.create("profilepicture").build())
                Label.of("Profilbild",TextInput.create("profilepicture-url", TextInputStyle.SHORT).build() )
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {

        UUID id = UUID.randomUUID();
        String profilename = event.getValue("profilename").getAsString().trim();

        String imageUrl = null;
        if (event.getValue("profilepicture") != null) {
            imageUrl = event.getValue("profilepicture").getAsString().trim();
        }

        Profile profile = new Profile(id.toString(), event.getMember(), profilename, false, imageUrl);
        ProfileRepository.createProfile(profile);
        event.reply(String.format("✅ Das Profil \"%s\" wurde erfolgreich erstellt.-# Verwende das Profil mit /profile use.",  profilename)).queue();
    }
}
