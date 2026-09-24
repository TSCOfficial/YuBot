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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AddProfileModal extends Modal {

    private Profile profile;

    private static final List<String> ALLOWED_DATATYPES = List.of(".jpeg", ".jpg", ".png");

    @Override
    public String getId() {
        return "add-profile-modal";
    }

    @Override
    public String getTitle() {
        if (profile != null) {
            return "Profil bearbeiten";
        }
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
        profilename.setPlaceholder("Max Muster");
        if (profile != null && !profile.name().isBlank()) {
            profilename.setValue(profile.name());
        }

        TextInput.Builder proxy = TextInput.create("proxy", TextInputStyle.SHORT);
        proxy.setRequired(false);
        proxy.setRequiredRange(1, 6);
        proxy.setPlaceholder("prx");

        if (profile != null && !profile.proxy().isBlank()) {
            proxy.setValue(profile.proxy());
        }

        TextInput.Builder profilepicture = TextInput.create("profilepicture-url", TextInputStyle.SHORT);
        profilepicture.setRequired(false);
        profilepicture.setPlaceholder("https://domain.org/exampleimage/xy.png");
        if (profile != null && !profile.profilePicture().isBlank()) {
            profilepicture.setValue(profile.profilePicture());
        }


        return List.of(
                TextDisplay.of("Dieses System erlaubt Personen mit Dissoziativer Identittsstörung, oder ähnliche Diagnosen, ein eigenes \"Profil\" für deren jeweilige Personen/Alters/Fronts zu kreieren."),
                Label.of(
                        "Profilname", profilename.build()
                ),
                Label.of(
                        "Proxy", "Verwende Proxy als Prefix, um die Nachricht mit diesem Profil zu versenden (Emoji / Text).", proxy.build()
                ),
//                Label.of("Profilbild", AttachmentUpload.create("profilepicture").build())
                Label.of("Profilbild", String.format("Erlaubte Bildformate: %s. Dateiupload wird nicht unterstützt.", String.join(", ", ALLOWED_DATATYPES)), profilepicture.build() )
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
        }

        String id = isEditMode ? profile.profileId() : UUID.randomUUID().toString();
        String profilename = event.getValue("profilename").getAsString().trim();
        String proxy = null;
        if (event.getValue("proxy") != null) {
            proxy = event.getValue("proxy").getAsString().trim();
            validateProxy(event.getMember(), proxy, profile);
        }

        String imageUrl = null;
        if (event.getValue("profilepicture-url") != null) {
            imageUrl = event.getValue("profilepicture-url").getAsString().trim();
            validateUrl(imageUrl);
        }

        int useCount = profile != null ? profile.useCount() : 0;
        boolean isInUse = profile != null && profile.isCurrentlyUsed();

        Profile newProfile = new Profile(id, event.getMember(), profilename, isInUse, imageUrl, proxy, useCount);
        if (isEditMode) {
            ProfileRepository.updateProfile(newProfile);
        } else {
            try {
                ProfileRepository.createProfile(newProfile);
            } catch (SQLException e) {
                if (e.getMessage().contains("Unique-Constraint")) {
                    throw new InvalidStateException(String.format("\"%s\" ist bereits vergeben.", profilename), "Verwende bitte eine andere Variation des Namens.");
                } else {
                    throw e;
                }

            }
        }
        event.reply(String.format("✅ Das Profil \"%s\" wurde erfolgreich %s.\n-# Wende es mit </profile show:1542519831729934447> an.",  profilename, isEditMode ? "aktualisiert" : "erstellt")).setEphemeral(true).queue();
    }

    /**
     * Validates the given image URL
     * <p>
     *     This uses the {@link #ALLOWED_DATATYPES} to check whether the URL ends with an allowed datatype
     * </p>
     * <p>
     *     If the URL is NOT valid, it throws an {@link InvalidStateException}, else it does nothing
     * </p>
     * @param url
     */
    private void validateUrl(@NonNull String url) {
        boolean isValid = true;
        if (url.isBlank()) {
            isValid = false;
        } else {
            boolean formatIsAllowed = ALLOWED_DATATYPES.stream().anyMatch(type -> {
                return url.endsWith(type) || url.contains(type + "?");
            });
            if (!formatIsAllowed) {
                isValid = false;
            }
        }



        if (!isValid) {
            throw new InvalidStateException("Profilbild konnte nicht definiert werden.", String.format("Dateiformat muss %s sein.", String.join(", ", ALLOWED_DATATYPES)));
        }
    }

    /**
     * Validates the proxy
     * <p>
     *     Aslong as the proxy was defined, it checks for an existing proxy of that member with the same name. The same parent-account can not have multiple profiles with the same proxy<br>
     *     If a profile is beeing edited, this is taken account for by removing the editing account from the linked profiles of the parent account
     * </p>
     * <p>
     *     If there are any conflicting proxies, it throws an {@link InvalidStateException}, else it does nothing
     * </p>
     * @param proxy
     */
    private void validateProxy(Member member, @NonNull String proxy, @Nullable Profile editingProfile) throws SQLException, ClassNotFoundException {
        if (!proxy.isBlank()) {
            List<Profile> linkedProfiles = ProfileRepository.getProfilesFromAccount(member);
            List<Profile> conflictingProfile = linkedProfiles.stream().filter(profile -> {
                if (editingProfile != null) {
                    return profile.proxy().equals(proxy) && !profile.profileId().equals(editingProfile.profileId());
                }
                return profile.proxy().equals(proxy);
            }).toList();
            if (!conflictingProfile.isEmpty()) {
                String stringConflicts = String.join(", ", conflictingProfile.stream().map(Profile::name).toList());
                throw new InvalidStateException(String.format("Proxy `%s` kann nicht gesetzt werden.", proxy), String.format("Diese Proxy wird bereits von %s verwendet.", stringConflicts));
            }
        }
    }
}
