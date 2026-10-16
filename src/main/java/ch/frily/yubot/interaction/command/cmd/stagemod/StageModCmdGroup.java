package ch.frily.yubot.interaction.command.cmd.stagemod;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;

import java.util.List;

public class StageModCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "stagemod";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new GiveStageModCmd(),
                new RemoveStageModCmd()
        );
    }
}
