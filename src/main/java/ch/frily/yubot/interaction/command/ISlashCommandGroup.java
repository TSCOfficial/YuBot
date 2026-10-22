package ch.frily.yubot.interaction.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.List;

/**
 * Represents a Slashcommand-group, containing at least one {@link ISlashSubcommand}
 * <p>
 *     <code>/command-group subcommand</code><br>
 * </p>
 */
public interface ISlashCommandGroup {

    String getName();

    String getDescription();

    default List<Permission> getDefaultPermissions() {
        return List.of();
    }

    /**
     * Get the direct subcommands associated with this command group
     * <p>
     *     <code>CommmandGroup / Subcommand</code>
     * </p>
     * <p>
     *      This does not include subcommand-groups nor their subcommands
     * </p>
     * @return
     */
    List<ISlashSubcommand> getSubcommands();

    default SlashCommandData build() {
        SlashCommandData slashCommand = Commands.slash(getName(), getDescription());

        if (!getDefaultPermissions().isEmpty())
            slashCommand.setDefaultPermissions(DefaultMemberPermissions.enabledFor(getDefaultPermissions()));

        getSubcommands().forEach(sub -> {
            // add normal subcommand
            if (sub.getSubcommands().isEmpty()) {
                SubcommandData subData = new SubcommandData(sub.getName(), sub.getDescription());
                if (!sub.getOptions().isEmpty()) {
                    subData.addOptions(sub.getOptions());
                }

                slashCommand.addSubcommands(subData);
            } else {
                SubcommandGroupData subGroupData = new SubcommandGroupData(sub.getName(), sub.getDescription());

                sub.getSubcommands().forEach(subSub -> {
                    SubcommandData subData = new SubcommandData(subSub.getName(), subSub.getDescription());
                    if (!subSub.getOptions().isEmpty()) {
                        subData.addOptions(subSub.getOptions());
                    }

                    subGroupData.addSubcommands(subData);
                });

                slashCommand.addSubcommandGroups(subGroupData);
            }
        });

        return slashCommand;
    }
}
