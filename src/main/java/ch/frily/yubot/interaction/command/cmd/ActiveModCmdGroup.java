package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.Permission;

import java.util.List;

public class ActiveModCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "activemod";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new ActiveModOptInCmd(),
                new ActiveModOptOutCmd(),
                new ActiveModKillCmd()
        );
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashCommandGroup.super.getDefaultPermissions();
    }
}
