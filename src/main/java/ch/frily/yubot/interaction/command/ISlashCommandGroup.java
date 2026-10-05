package ch.frily.yubot.interaction.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.List;

public interface ISlashCommandGroup {

    String getName();

    String getDescription();

    default List<Permission> getDefaultPermissions() {
        return List.of();
    }

    List<ISlashSubcommand> getSubcommands();
}
