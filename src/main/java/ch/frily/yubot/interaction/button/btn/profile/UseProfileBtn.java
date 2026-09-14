package ch.frily.yubot.interaction.button.btn.profile;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.interaction.button.Button;
import lombok.Setter;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public class UseProfileBtn extends Button {
    private boolean disabled = false;

    public void disable(boolean disabled) {
        this.disabled = disabled;
    }
    public String getId() {
        return "use-profile-btn";
    }

    @Override
    public String getLabel() {
        return "Profil anwenden";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        String profileId = getArgument(event.getComponentId(),"p");
        Profile profile = ProfileRepository.getProfileById(profileId);
        ProfileRepository.selectProfile(profile);
        event.reply(String.format("✅ Profil **%s** wird nun angewendet.\n-# Du sendest absofort deine Nachrichten als %s", profile.name(), profile.name()));
    }
}
