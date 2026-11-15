package ch.frily.yubot.service.forummanager.slashcommand;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;

import java.util.List;

/**
 * <code>/forum</code> command group
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class ForumCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "forum";
    }

    @Override
    public String getDescription() {
        return "Verwalte Forum-Channels";
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new ForumCreatePostCmd()
        );
    }
}
