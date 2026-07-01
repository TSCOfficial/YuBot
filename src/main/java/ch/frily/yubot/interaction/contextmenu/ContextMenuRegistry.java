package ch.frily.yubot.interaction.contextmenu;

import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.interaction.contextmenu.ctxmenu.ModticketCtxMenu;
import ch.frily.yubot.util.Util;
import javassist.NotFoundException;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextMenuRegistry {

    private static ContextMenuRegistry instance;

    private final Map<String, IContextMenu> contextMenus = new HashMap<>();

    public static ContextMenuRegistry getInstance(){
        if (instance == null) {
            instance = new ContextMenuRegistry();
        }
        return instance;
    }

    public void loadContextMenus(){
        List<IContextMenu> contextMenus = List.of(
                new ModticketCtxMenu()
        );

        contextMenus.forEach(ctxMenu -> this.contextMenus.put(ctxMenu.getName(), ctxMenu));
    }

    /**
     * Prepare the context menus for the registry
     * @return list of prepared context menus
     */
    public List<CommandData> prepareForRegistry(){
        return contextMenus.values().stream().map(this::buildContextMenu).toList();
    }

    private CommandData buildContextMenu(IContextMenu contextMenu){
        CommandData commandData = Commands.context(contextMenu.getType(), contextMenu.getName());
        if (!contextMenu.getDefaultPermissions().isEmpty()) {
            commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(contextMenu.getDefaultPermissions()));
        }
        return commandData;
    }

    /**
     * Dispatch the event from an eventlistener to the appropriate interaction executor.
     * <br>
     * Handles both {@link Command.Type#USER USER} and {@link Command.Type#MESSAGE MESSAGE} context menus.
     * @param event the generic context interaction event fired by discord
     */
    public void dispatchInteractionEvent(GenericContextInteractionEvent<?> event) throws NotFoundException, SQLException, ClassNotFoundException {
        IContextMenu contextMenu = contextMenus.get(event.getName());

        if (contextMenu == null) {
            throw new NotFoundException(String.format("Kontextmenü '%s' konnte nicht gefunden werden.", event.getName()));
        }

        // Check if user is allowed to execute command
        if (!Util.isAdministrator(event.getMember()) && !contextMenu.getAllowedRoles().isEmpty() && contextMenu.getAllowedRoles().stream().noneMatch(role -> event.getMember().getRoles().contains(role))) {
            throw new PermissionDeniedException(String.format("Nur Mitglieder\\*innen mit einer der folgenden Rollen können dieses Kontextmenü ausführen: %s", String.join(", ", contextMenu.getAllowedRoles().stream().map(role -> role.getAsMention()).toList())));
        }

        if (contextMenu instanceof IUserContextMenu userContextMenu && event instanceof UserContextInteractionEvent userEvent) {
            userContextMenu.execute(userEvent);
        } else if (contextMenu instanceof IMessageContextMenu messageContextMenu && event instanceof MessageContextInteractionEvent messageEvent) {
            messageContextMenu.execute(messageEvent);
        } else {
            throw new IllegalStateException(String.format("Der Typ des Kontextmenüs '%s' passt nicht zum ausgelösten Event.", event.getName()));
        }
    }
}
