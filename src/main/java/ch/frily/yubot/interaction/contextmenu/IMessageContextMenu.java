package ch.frily.yubot.interaction.contextmenu;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

public interface IMessageContextMenu extends IContextMenu{

    /**
     * Type of the context menu
     * <br>
     * <b>DO NOT CHANGE THIS</b>
     */
    @Override
    default Command.Type getType() {
        return Command.Type.MESSAGE;
    }

    void execute(@NotNull MessageContextInteractionEvent event);

}
