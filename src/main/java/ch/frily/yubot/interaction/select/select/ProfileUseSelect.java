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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ProfileUseSelect implements IStringSelect {

    private static final String DEFAULT_ACCOUNT_KEY = "default-profile";
    private static final String ACTIVE_TAG =
            "<:active1:1527044015927721984><:active2:1527044016942616748><:active3:1527044018276536403>";

    @Setter
    private Profile profile;

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
            List<Profile> profiles = ProfileRepository.getProfilesFromAccount(member); // -1 because of default profile
            profiles = ProfileRepository.orderByUsage(profiles).stream().limit(MAX_SELECT_OPTIONS - 1).toList();

            List<SelectOption> options = new ArrayList<>();
            profiles.forEach(profile -> {
                String proxy = !profile.proxy().isBlank() ? " (" + profile.proxy() + ")" : "";
                String isInUse = profile.isCurrentlyUsed() ? "🟢 " : "";
                options.add(SelectOption.of(isInUse + profile.name() + proxy, profile.profileId()));
            });

            options.addFirst(SelectOption.of(String.format("Standardprofil (%s)", member.getEffectiveName()), DEFAULT_ACCOUNT_KEY));
            return options;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<SelectOption> getDefaultOptions() {
        if (profile != null) {
            return getOptions().stream().filter(option -> option.getValue().equals(profile.profileId())).toList();
        }
        return getOptions().stream().filter(option -> option.getValue().equals(DEFAULT_ACCOUNT_KEY)).toList();
    }

    @Override
    public void execute(@NonNull StringSelectInteractionEvent event) throws SQLException, ClassNotFoundException {
        String selectedValue = event.getSelectedOptions().getFirst().getValue();

        ProfilContainer profileContainer = new ProfilContainer(event.getMember());

        if (!selectedValue.equals(DEFAULT_ACCOUNT_KEY)) {
            Profile selectedProfile = ProfileRepository.getProfileById(selectedValue);
            profileContainer.setProfile(selectedProfile);
        }


        profileContainer.buildAsync().thenAccept(container -> {
            event.editComponents(container.build()).useComponentsV2().queue();
        }).exceptionally(e -> {
            return ExceptionHandler.fail(e);
        });

    }
}
