package ch.frily.yubot.service.postoffice.slashcommand;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;

import java.util.List;

public class StatusCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Verwalte den Status des Bots";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new StatusResetCmd(),
                new StatusSetCmd()
        );
    }
}
