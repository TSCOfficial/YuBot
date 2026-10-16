package ch.frily.yubot.interaction.command.cmd.stagemod;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RemoveStageModCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Entziehe die Stage-Moderations-Rolle einer Person.";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        OptionMapping userOption = Objects.requireNonNull(event.getOption("user"));
        Role stageModRole = Objects.requireNonNull(event.getGuild()).getRoleById(1559970596362911804L);
        event.getGuild().addRoleToMember(userOption.getAsMember(), stageModRole).queue();
        event.reply(String.format("%s wurde die <@&1559980125381070929>-Rolle entzogen.", userOption.getAsMember().getAsMention())).setEphemeral(true).queue();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "Die Person welche die Rolle entzogen werden soll.")
        );
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_EVENTLEITUNG,
                EnvKey.ROLE_STVEVENTLEITUNG
        ).map(EnvResolver::getRoleById).toList();
    }
}
