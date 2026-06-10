package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.Permission;

import java.util.List;

public class ClosureCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "closure";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new ClosureOptInCmd(),
                new ClosureOptOutCmd()
        );
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashCommandGroup.super.getDefaultPermissions();
    }
}
