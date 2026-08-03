package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.feature.TicketManager;
import ch.frily.yubot.interaction.modal.IModal;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.feature.TicketType;
import ch.frily.yubot.feature.TicketTypeGroup;
import ch.frily.yubot.interaction.modal.Modal;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

@Slf4j
public class TypeSelectorModal extends Modal {

    @Setter
    private TicketTypeGroup typeGroup;

    @Override
    public String getTitle() {
        return "Ticketsupport";
    }

    @Override
    public String getId() {
        return "modal:ticket-type-modal";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {
        List<ModalTopLevelComponent> components = new ArrayList<>();
        List<TicketType> types = Arrays.stream(TicketType.values()).filter(type -> type.getGroup() == typeGroup).toList();

        StringSelectMenu.Builder selectMenuBuilder = StringSelectMenu.create("select-menu:ticket-type-selector");
        types.forEach(type -> {
            if (type.getSelectDescription() != null) {
                selectMenuBuilder.addOption(type.getLabel(), type.getId(), type.getSelectDescription());
            }
        });
        selectMenuBuilder.setMinValues(1);
        selectMenuBuilder.setMaxValues(1);
        selectMenuBuilder.setRequired(true);
        selectMenuBuilder.setDefaultOptions(selectMenuBuilder.getOptions().getFirst());

        components.add(Label.of("Ticketart", selectMenuBuilder.build()));

        return components;
    }

    @Override
    public void execute(@NotNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        event.deferReply(true).queue();
        TicketType ticketType = Arrays.stream(TicketType.values()).filter(type ->
                Objects.equals(type.getId(), event.getValue("select-menu:ticket-type-selector").getAsStringList().getFirst())
        ).findFirst().orElseThrow(() -> new IllegalStateException("Tickettyp ist ungültig."));

        TicketManager.getInstance().createTicket(ticketType, event.getMember(), channel -> {
            event.getHook().editOriginal("Dein Ticket wurde erstellt: " + channel.getAsMention()).queue();
        });
    }
}
