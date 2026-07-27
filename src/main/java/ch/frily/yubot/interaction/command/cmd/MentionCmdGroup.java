package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;

import java.util.List;

public class MentionCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "mention";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new MentionModCmd(),
                new MentionSupCmd(),
                new MentionEventCmd()
        );
    }
}
