package ch.frily.yubot.service.eventtool.slashcommand;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;

import java.util.List;

public class StageModCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "stagemod";
    }

    @Override
    public String getDescription() {
        return "Verwalte wer für das kommende event stage-moderator*in ist";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new GiveStageModCmd(),
                new RemoveStageModCmd()
        );
    }
}
