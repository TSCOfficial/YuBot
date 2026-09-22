package ch.frily.yubot.interaction.button.btn.profile;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.modal.modal.AddProfileModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public class EditProfileBtn extends Button {

    @Override
    public String getId() {
        return "edit-profile-btn";
    }

    @Override
    public String getLabel() {
        return "Profil bearbeiten";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        String profileId = getArgument(event.getComponentId(), "p");
        Profile profile = ProfileRepository.getProfileById(profileId);
        AddProfileModal modal =  new AddProfileModal();
        modal.setProfile(profile);
        event.replyModal(modal.build()).queue();
    }
}
