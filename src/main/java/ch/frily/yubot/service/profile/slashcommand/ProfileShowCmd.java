package ch.frily.yubot.service.profile.slashcommand;

import ch.frily.yubot.container.profile.ProfilContainer;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.service.profile.Profile;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProfileShowCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "Öffne dein Profil";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "profile", "Eins deiner Profile welches du anschauen möchtest", false, true),
                new OptionData(OptionType.USER, "user", "Profil einer anderen Person einsehen")
        );
    }

    @Override
    public Map<String, List<Command.Choice>> getAutocomplete(CommandAutoCompleteInteractionEvent event) throws SQLException {
        List<Profile> existingProfiles = ProfileRepository.getProfilesFromAccount(event.getMember());
        existingProfiles = ProfileRepository.orderByUsage(existingProfiles);
        if (existingProfiles.isEmpty()) {
            return Map.of();
        } else {
            List<Command.Choice> choices = existingProfiles.stream().map(profile -> {
                String proxy = !profile.proxy().isBlank() ? " (" + profile.proxy() + ")" : "";
                String isInUse = profile.isCurrentlyUsed() ? "🟢 " : "";
                return new Command.Choice(isInUse + profile.name() + proxy, profile.profileId());
            }).toList();
            return Map.of("profile", choices);
        }
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException {
        event.deferReply(true).queue();
        Member initiator = event.getMember();
        Member target = event.getOption("user") != null ? event.getOption("user").getAsMember() : null;
        ProfilContainer container = new ProfilContainer(initiator);

        ProfileRepository.getCurrentUserProfile(initiator).ifPresent(container::setProfile);
        if (target != null) {
            container.setTarget(target);
        } else if (event.getOption("profile") != null) {
            Profile selectedProfile = ProfileRepository.getProfileById(event.getOption("profile").getAsString());
            container.setProfile(selectedProfile);
        }


        container.buildAsync().thenAccept(builtContainer -> {
            MessageCreateData message = new MessageCreateBuilder()
                    .useComponentsV2()
                    .setComponents(builtContainer.build())
                    .addFiles(builtContainer.getProfileBanner())
                    .build();
            event.getHook().sendMessage(message).queue();
        });
    }
}
