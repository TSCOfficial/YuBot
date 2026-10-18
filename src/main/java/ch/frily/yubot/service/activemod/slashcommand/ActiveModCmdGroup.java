package ch.frily.yubot.service.activemod.slashcommand;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;

import java.util.List;

public class ActiveModCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "activemod";
    }

    @Override
    public String getDescription() {
        return "Steuere die Serveröffnung.";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new ActiveModOptInCmd(),
                new ActiveModOptOutCmd(),
                new ActiveModKillCmd(),
                new ActiveModStatisticCmd(),
                new ActiveModControlCmd()
        );
    }
}
