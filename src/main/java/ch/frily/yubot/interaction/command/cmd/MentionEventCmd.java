package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class MentionEventCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "event";
    }

    @Override
    public String getDescription() {
        return "Erwähne das Event-Team mittels der Event-Team-Rolle";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        Role supportRole = EnvResolver.getRoleById(EnvKey.ROLE_EVENT);
        event.reply(String.format("%s, ihr seid gefragt ✨", supportRole.getAsMention())).queue();
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_EVENT
        ).map(EnvResolver::getRoleById).toList();
    }
}
