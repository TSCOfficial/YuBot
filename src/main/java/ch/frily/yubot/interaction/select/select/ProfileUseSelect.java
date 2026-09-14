package ch.frily.yubot.interaction.select.select;

import ch.frily.yubot.container.profile.ProfilContainer;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.interaction.select.IStringSelect;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ProfileUseSelect implements IStringSelect {

    @Setter
    private Profile profile;

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
            List<Profile> profiles = ProfileRepository.getProfilesFromAccount(profile.parentAccount());
            return profiles.stream().map(profile -> {
                return SelectOption.of(profile.name(), profile.profileId());
            }).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<SelectOption> getDefaultOptions() {
        if (profile != null) {
            return List.of(SelectOption.of(profile.name(), profile.profileId()));
        }
        return List.of();
    }

    @Override
    public void execute(@NonNull StringSelectInteractionEvent event) throws SQLException, ClassNotFoundException {
        String selectedValue = event.getSelectedOptions().getFirst().getValue();
        Profile selectedProfile = ProfileRepository.getProfileById(selectedValue);
        ProfilContainer profileContainer = new ProfilContainer(event.getMember());
        profileContainer.setProfile(selectedProfile);

        profileContainer.buildAsync().thenAccept(container -> {
            event.editComponents(container.build()).useComponentsV2().queue();
        }).exceptionally(e -> {
            return ExceptionHandler.fail(e);
        });

    }
}
