package ch.frily.yubot.interaction.button;

import ch.frily.yubot.interaction.button.btn.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

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
                new TicketCloseRequestBtn(),
                new TicketDeleteBtn(),
                new TicketCloseRequestAcceptBtn(),
                new TicketCloseRequestRejectBtn()
        );
        rawButtons.forEach(btn -> {
            String idOrUrl = btn.getId();
            if (btn.getStyle() == ButtonStyle.LINK && btn.getUrl() != null) {
                idOrUrl = btn.getUrl();
            }
            buttons.put(idOrUrl, btn);
        });
    }

    public void dispatchButtonInteraction(ButtonInteractionEvent event){
        String idOrUrl = event.getButton().getCustomId();
        if (event.getButton().getStyle() == ButtonStyle.LINK && event.getButton().getUrl() != null) {
            idOrUrl = event.getButton().getUrl();
        }
        buttons.get(idOrUrl).execute(event);
    }
}
