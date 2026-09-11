package ch.frily.yubot.interaction.command.cmd.profile;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
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
        Map<String, List<Command.Choice>> choices = new HashMap<>();
        List<Command.Choice> choiceList = ProfileRepository.getProfilesFromAccount(event.getMember()).stream().map(profile -> {
            return new Command.Choice(profile.name(), profile.profileId());
        }).toList();
        choiceList.addFirst(new Command.Choice(String.format("Standard (%d)", event.getMember().getEffectiveName()), "default"));
        choices.put("profile", choiceList);
        return choices;
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {

    }
}
