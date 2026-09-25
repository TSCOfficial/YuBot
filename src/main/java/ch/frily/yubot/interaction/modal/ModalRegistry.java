package ch.frily.yubot.interaction.modal;

import ch.frily.yubot.interaction.ArgumentComponent;
import ch.frily.yubot.interaction.modal.modal.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModalRegistry {

    private static ModalRegistry instance;

    private Map<String, Modal> modals = new HashMap<>();

    public static ModalRegistry getInstance(){
        if (instance == null) {
            instance = new ModalRegistry();
        }
        return instance;
    }

    public void register(List<Modal> modals) {
        modals.forEach(modal -> {
            this.modals.put(modal.getId(), modal);
            log.info("Loaded modal '{}'.", modal.getClass().getSimpleName());
        });
    }

    public void loadModals(){
        List<Modal> rawModals = List.of(
                new TypeSelectorModal(),
                new AbsenceAddModal(),
                new TicketSummaryModal(),
                new SelectActiveModSendTypeModal(),
                new AddProfileModal(),
                new EditWebhookMessageModal()
        );

        rawModals.forEach(modal -> {
            this.modals.put(modal.getId(), modal);
            log.info("Loaded modal '{}'.", modal.getClass().getSimpleName());
        });
    }

    public void dispatchModalInteraction(ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        String id = ArgumentComponent.extractId(event.getModalId());
        log.info("Dispatching modal with id {}", id);
        Modal modal = modals.get(id);
        if (modal == null) {
            throw new NullPointerException(String.format("Modal '%s' konnte nicht gefunden werden.", event.getModalId()));
        }
        modal.execute(event);
    }
}
