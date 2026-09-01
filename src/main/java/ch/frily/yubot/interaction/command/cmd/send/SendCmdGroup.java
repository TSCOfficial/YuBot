package ch.frily.yubot.interaction.command.cmd.send;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.Permission;

import java.util.List;

public class SendCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "send";
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return List.of(Permission.ADMINISTRATOR);
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new SendContainerCmd(),
                new SendEmbedCmd(),
                new SendDynamicMsgCmd()
        );
    }
}
