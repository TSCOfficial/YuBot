package ch.frily.yubot.interaction.button.btn.profile;

import ch.frily.yubot.container.profile.ProfilContainer;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.InvalidStateException;
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
        String profileId = null;
        try {
            profileId = getArgument(event.getComponentId(), "p");
        } catch (InvalidStateException e) {
            // ignore missing argument (sets to default profile)
        }

        ProfilContainer newProfileView = new ProfilContainer(event.getMember());
        String reply = "";
        if (profileId == null) {
            ProfileRepository.unselectProfiles(event.getMember());
            reply = "✅ Profilauswahl zurückgesetzt.\n-# Du verwendest nun kein Profil mehr.";
        } else {
            Profile profile = ProfileRepository.getProfileById(profileId);
            ProfileRepository.selectProfile(profile);
            newProfileView.setProfile(profile);
            reply = String.format("✅ Profil **%s** wird nun angewendet.\n-# Du sendest absofort deine Nachrichten als %s", profile.name(), profile.name());
        }




        newProfileView.buildAsync().thenAccept(profileContainer -> {
            event.getMessage().editMessageComponents(profileContainer.build()).useComponentsV2().queue();
        });

        event.reply(reply).setEphemeral(true).queue();
    }
}
