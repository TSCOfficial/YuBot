package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EventControlCmdGroup implements ISlashCommandGroup {
    @Override
    public String getName() {
        return "event";
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashCommandGroup.super.getDefaultPermissions();
    }

    @Override
    public List<ISlashSubcommand> getSubcommands() {
        return List.of(
                new EventOpenCmd()
        );
    }
}
