package ch.frily.yubot.interaction.select;

import ch.frily.yubot.interaction.select.select.ActiveModTrackingDetailSelect;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SelectRegistry {

    private static SelectRegistry instance;

    private Map<String, ISelect> selects = new HashMap<>();

    public static SelectRegistry getInstance(){
        if (instance == null) {
            instance = new SelectRegistry();
        }
        return instance;
    }

    public void loadSelects(){
        List<ISelect> rawModals = List.of(
                new ActiveModTrackingDetailSelect()
        );

        rawModals.forEach(select -> {
            log.info("Loaded select with id {}", select.getId());
            selects.put(select.getId(), select);
        });
    }

    public void dispatchSelectInteraction(GenericSelectMenuInteractionEvent<?, ?> event) throws SQLException, ClassNotFoundException, NullPointerException {
        ISelect select = selects.get(event.getInteraction().getComponentId());
        if (select == null) {
            throw new NullPointerException(String.format("Select '%s' konnte nicht gefunden werden.", event.getId()));
        }

        if (select instanceof IStringSelect stringSelect && event instanceof StringSelectInteractionEvent stringEvent) {
            stringSelect.execute(stringEvent);
        }
    }
}
