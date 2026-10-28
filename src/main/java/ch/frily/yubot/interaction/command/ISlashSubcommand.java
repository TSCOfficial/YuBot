package ch.frily.yubot.interaction.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

import static org.reflections.Reflections.log;

/**
 * Represents a sub-command
 * <p>
 *     <code>/command</code>
 * </p>
 * <p>
 *     A subcommand can contain their own subcommand-group for one level.<br>
 *     <code>/command subcommand subcommand</code> while the second subcommand automatically a group is
 * </p>
 */
public interface ISlashSubcommand extends ISlashCommand {

    /**
     * Gets the subcommands of this subcommand
     * @return
     */
    default List<ISlashSubcommand> getSubcommands() {
        return List.of();
    }

    @Override
    default void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        log.error("Missing execution code for subcommand '{}'!", event.getFullCommandName());
        event.reply("Missing execution code for subcommand!").setEphemeral(true).queue();
    };
}
