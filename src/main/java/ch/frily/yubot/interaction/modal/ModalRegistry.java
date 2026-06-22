package ch.frily.yubot.interaction.modal;

import ch.frily.yubot.interaction.modal.modal.TypeSelectorModal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModalRegistry {

    private static ModalRegistry instance;

    private Map<String, IModal> modals = new HashMap<>();

    public static ModalRegistry getInstance(){
        if (instance == null) {
            instance = new ModalRegistry();
        }
        return instance;
    }

    public void loadModals(){
        List<IModal> rawModals = List.of(
                new TypeSelectorModal()
        );

        rawModals.forEach(modal -> {
            log.info("Loaded modal with id {}", modal.getId());
            modals.put(modal.getId(), modal);
        });
    }

    public void dispatchModalInteraction(ModalInteractionEvent event) throws SQLException, ClassNotFoundException {
        log.debug("Modal interaction dispatched: {}", event.getModalId());
        modals.get(event.getModalId()).execute(event);
    }
}
