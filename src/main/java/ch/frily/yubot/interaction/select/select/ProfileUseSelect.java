package ch.frily.yubot.interaction.select.select;

import ch.frily.yubot.container.profile.ProfilContainer;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.interaction.select.IStringSelect;
import lombok.Setter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProfileUseSelect implements IStringSelect {

    @Setter
    private Member member;

    @Override
    public String getId() {
        return "show-profile-select";
    }

    @Override
    public String getPlaceholder() {
        return "Profil auswählen";
    }

    @Override
    public List<SelectOption> getOptions() {
        try {
            List<Profile> profiles = ProfileRepository.getProfilesFromAccount(member);
            return profiles.stream().map(profile -> {
                return SelectOption.of(profile.name(), profile.profileId());
            }).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<SelectOption> getDefaultOptions() {
        try {
            Optional<Profile> currentProfile = ProfileRepository.getCurrentUserProfile(member);
            if  (currentProfile.isPresent()) {
                return List.of(SelectOption.of(currentProfile.get().name(), currentProfile.get().profileId()));
            } else {
                return List.of();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            return List.of();
        }
    }

    @Override
    public void execute(@NonNull StringSelectInteractionEvent event) throws SQLException, ClassNotFoundException {
        String selectedValue = event.getSelectedOptions().getFirst().getValue();
        Profile selectedProfile = ProfileRepository.getProfileById(selectedValue);
        ProfilContainer profileContainer = new ProfilContainer(event.getMember());
        profileContainer.setProfile(selectedProfile);

        event.editSelectMenu(profileContainer.build()).queue(); // failes due to components v2 not being able to resolve itself somehow? reference to dynamic msg .update()

    }
}
