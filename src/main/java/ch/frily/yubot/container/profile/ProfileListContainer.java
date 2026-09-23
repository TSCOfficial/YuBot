package ch.frily.yubot.container.profile;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.interaction.select.ISelect;
import ch.frily.yubot.interaction.select.select.ProfileUseSelect;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.separator.Separator;

import java.util.ArrayList;
import java.util.List;

public class ProfileListContainer extends Container {

    public ProfileListContainer(List<Profile> profiles) {
        addFormatedText("# Deine Profile (%d)", profiles.size());

        if (profiles.isEmpty()) {
            addTextDisplay("Keine Profile gefunden.");
            return;
        }

        List<String> preparedProfileStrings = profiles.stream().map((profile) -> {
            String proxy = !profile.proxy().isBlank() ? "(`" + profile.proxy() + "`)" : "";
            return String.format("1. %s %s", profile.name(), proxy);
        }).toList();

        String listedProfiles = String.join("\n", preparedProfileStrings);
        addTextDisplay(listedProfiles);

        if (!profiles.isEmpty()) {
            addLineSeparator(Separator.Spacing.SMALL);
            ProfileUseSelect profileUseSelect = new ProfileUseSelect();
            profileUseSelect.setMember(profiles.getFirst().parentAccount());

            addComponent(
                    ActionRow.of(
                            profileUseSelect.build()
                    )
            );

            if (profiles.size() >= ISelect.MAX_SELECT_OPTIONS - 1) { // -1 because of the default profile in the selection
                addFormatedText("-# Es können nur 25 Optionen angezeigt werden. Verwende </profile show:1542519831729934447> um ein spezifisches Profil anzusehen.");
            }
        }
    }
}
