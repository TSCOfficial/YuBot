package ch.frily.yubot.interaction.contextmenu;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface IUserContextMenu extends IContextMenu{

    /**
     * Type of the context menu
     * <br>
     * <b>DO NOT CHANGE THIS</b>
     */
    @Override
    default Command.Type getType() {
        return Command.Type.USER;
    }

    void execute(@NotNull UserContextInteractionEvent event) throws SQLException, ClassNotFoundException;

}
