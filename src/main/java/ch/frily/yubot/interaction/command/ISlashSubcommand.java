package ch.frily.yubot.interaction.command;

import java.util.List;

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
}
