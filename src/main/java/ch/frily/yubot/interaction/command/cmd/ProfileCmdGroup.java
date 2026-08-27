package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;

import java.util.List;

public class ProfileCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
            new ProfileShowCmd(),
            new ProfileSettingCmd()
        );
    }
}
