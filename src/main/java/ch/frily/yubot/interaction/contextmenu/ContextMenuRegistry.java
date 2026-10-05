package ch.frily.yubot.interaction.contextmenu;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.util.Util;
import javassist.NotFoundException;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ContextMenuRegistry {

    private static ContextMenuRegistry instance;

    private final Map<String, IContextMenu> contextMenus = new HashMap<>();

    public static ContextMenuRegistry getInstance(){
        if (instance == null) {
            instance = new ContextMenuRegistry();
        }
        return instance;
    }

    public List<CommandData> register(List<IContextMenu> contextMenus){
        contextMenus.forEach(contextMenu -> {
            this.contextMenus.put(contextMenu.getName(), contextMenu);
        });
        return contextMenus.stream().map(IContextMenu::build).toList();
    }

    /**
     * Dispatch the event from an eventlistener to the appropriate interaction executor.
     * <br>
     * Handles both {@link Command.Type#USER USER} and {@link Command.Type#MESSAGE MESSAGE} context menus.
     * @param event the generic context interaction event fired by discord
     */
    public void dispatchInteractionEvent(@NotNull GenericContextInteractionEvent<?> event) throws NotFoundException, SQLException, ClassNotFoundException {
        IContextMenu contextMenu = contextMenus.get(event.getName());

        if (contextMenu == null) {
            throw new NotFoundException(String.format("Kontextmenü '%s' konnte nicht gefunden werden.", event.getName()));
        }

        // Check if user is allowed to execute command
        if (!Util.isAdministrator(Objects.requireNonNull(event.getMember())) && !contextMenu.getAllowedRoles().isEmpty() && contextMenu.getAllowedRoles().stream().noneMatch(role -> event.getMember().getRoles().contains(role))) {
            throw new PermissionDeniedException(String.format("Nur Mitglieder\\*innen mit einer der folgenden Rollen können dieses Kontextmenü ausführen: %s", String.join(", ", contextMenu.getAllowedRoles().stream().map(IMentionable::getAsMention).toList())));
        }

        if (contextMenu instanceof IUserContextMenu userContextMenu && event instanceof UserContextInteractionEvent userEvent) {
            userContextMenu.execute(userEvent);
        } else if (contextMenu instanceof IMessageContextMenu messageContextMenu && event instanceof MessageContextInteractionEvent messageEvent) {
            messageContextMenu.execute(messageEvent);
        } else {
            throw new InvalidStateException(String.format("Der Typ des Kontextmenüs '%s' passt nicht zum ausgelösten Event.", event.getName()));
        }
    }
}
