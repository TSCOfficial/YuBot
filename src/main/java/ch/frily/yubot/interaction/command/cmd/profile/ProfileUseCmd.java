package ch.frily.yubot.interaction.command.cmd.profile;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileUseCmd implements ISlashSubcommand {
    private static final String DEFAULT_ACCOUNT_KEY = "default-account";

    @Override
    public String getName() {
        return "use";
    }

    @Override
    public String getDescription() {
        return "Wende ein Profil an";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "profile", "Das Profil welches du anwenden möchtest", true, true)
        );
    }

    @Override
    public Map<String, List<Command.Choice>> getAutocomplete(CommandAutoCompleteInteractionEvent event) throws SQLException, ClassNotFoundException {
        List<Profile> existingProfiles = ProfileRepository.getProfilesFromAccount(event.getMember());
        if (existingProfiles.isEmpty()) {
            return Map.of();
        } else {
            List<Command.Choice> choices = new ArrayList<>();
            existingProfiles.forEach(profile -> {
                String proxy = !profile.proxy().isBlank() ? " (" + profile.proxy() + ")" : "";
                String isInUse = profile.isCurrentlyUsed() ? "🟢 " : "";
                choices.add(new Command.Choice(isInUse + profile.name() + proxy, profile.profileId()));
            });
            choices.addFirst(new Command.Choice(String.format("Standardprofil (%s)", event.getMember().getEffectiveName()), DEFAULT_ACCOUNT_KEY));
            return Map.of("profile", choices);
        }
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        String profileId = event.getOption("profile").getAsString();
        String reply = "";

        try {
            Profile profile = ProfileRepository.getProfileById(profileId);
            ProfileRepository.selectProfile(profile);
            reply = String.format("✅ Profil **%s** wird nun angewendet.\n-# Du sendest absofort deine Nachrichten als %s", profile.name(), profile.name());
        } catch (NotFoundException e) {
            ProfileRepository.unselectProfiles(event.getMember());
            reply = "✅ Profilauswahl zurückgesetzt.\n-# Du verwendest nun kein Profil mehr.";
        }

        event.reply(reply).setEphemeral(true).queue();
    }
}
