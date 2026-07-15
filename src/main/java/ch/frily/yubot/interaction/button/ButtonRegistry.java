package ch.frily.yubot.interaction.button;

import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.interaction.button.btn.*;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ButtonRegistry {

    private static ButtonRegistry instance;

    // id/url, Button
    public Map<String, IButton> buttons = new HashMap<>();

    public static ButtonRegistry getInstance(){
        if (instance == null) {
            instance = new ButtonRegistry();
        }
        return instance;
    }

    public void loadButtons() {
        List<IButton> rawButtons = List.of(
                new TicketPanelSupportBtn(),
                new TicketPanelAwarenessBtn(),
                new TicketPanelBewerbungBtn(),
                new TicketCloseRequestBtn(),
                new TicketDeleteBtn(),
                new TicketCloseRequestAcceptBtn(),
                new TicketCloseRequestRejectBtn(),
                new ActiveModActivityProveBtn(),
                new ActiveModActivityRejectBtn(),
                new AddServeropenRoleBtn(),
                new ActiveModStatisticGoToHomeBtn()
        );
        rawButtons.forEach(btn -> {
            String idOrUrl = btn.getId();
            if (btn.getStyle() == ButtonStyle.LINK && btn.getUrl() != null) {
                idOrUrl = btn.getUrl();
            }
            buttons.put(idOrUrl, btn);
        });
    }

    public void dispatchButtonInteraction(ButtonInteractionEvent event) throws SQLException, IllegalStateException, ClassNotFoundException {
        String idOrUrl = event.getButton().getCustomId();
        if (event.getButton().getStyle() == ButtonStyle.LINK && event.getButton().getUrl() != null) {
            idOrUrl = event.getButton().getUrl();
        }
        IButton button = buttons.get(idOrUrl);
        if (button == null) {
            throw new IllegalStateException(String.format("Der Button '%s' konnte nicht gefunden werden.", idOrUrl));
        }

        // Check if user is allowed to execute command
        if (!Util.isAdministrator(event.getMember()) && !button.getAllowedRoles().isEmpty() && button.getAllowedRoles().stream().noneMatch(role -> event.getMember().getRoles().contains(role))) {
            throw new PermissionDeniedException(String.format("Nur Mitglieder\\*innen mit einer der folgenden Rollen können diesen Button verwenden: %s", String.join(", ", button.getAllowedRoles().stream().map(role -> role.getAsMention()).toList())));
        }

        button.execute(event);
    }
}
