package ch.frily.yubot.interaction.command.cmd.profile;

import ch.frily.yubot.container.profile.ProfilContainer;
import ch.frily.yubot.container.profile.ProfileListContainer;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.feature.profile.Profile;
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
public class ProfileListCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Liste all deine Profile auf";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        event.deferReply(true).queue();
        List<Profile> existingProfiles = ProfileRepository.getProfilesFromAccount(event.getMember());
        existingProfiles = ProfileRepository.orderByUsage(existingProfiles);
        ProfileListContainer container = new ProfileListContainer(existingProfiles);

        MessageCreateData message = new MessageCreateBuilder()
                .useComponentsV2()
                .setComponents(container.build())
                .build();
        event.getHook().sendMessage(message).queue();
    }
}
