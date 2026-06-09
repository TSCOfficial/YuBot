package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.interaction.command.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EventChannelControllCmd implements ISlashCommand {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {

    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashCommand.super.getDefaultPermissions();
    }
}
