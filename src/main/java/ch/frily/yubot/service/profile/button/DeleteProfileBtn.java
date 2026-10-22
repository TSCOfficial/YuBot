package ch.frily.yubot.service.profile.button;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.profile.Profile;
import ch.frily.yubot.service.profile.modal.DeleteProfileModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public class DeleteProfileBtn extends Button {

    @Override
    public String getId() {
        return "del-profile-btn";
    }

    @Override
    public String getLabel() {
        return "Profil löschen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.DANGER;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException {
        String profileId = getArgument(event.getComponentId(), "p");
        Profile profile = ProfileRepository.getProfileById(profileId);
        event.replyModal(new DeleteProfileModal().setProfile(profile).build()).queue();
    }
}
