package ch.frily.yubot.service.ticket.modal;

import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.TicketService;
import ch.frily.yubot.service.ticket.TicketTeam;
import ch.frily.yubot.service.ticket.TicketType;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.checkbox.Checkbox;
import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroup;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModTicketSelectorModal extends Modal {

    /**
     * The message that triggered the modal (if messageContextMenu)
     * <p>
     *     This is used to determine whether the message should be taken in account for or not
     * </p>
     */
    private Message message;

    public void setMessage(Message message) {
        this.message = message;
        addArgument("msg", message.getId());
    }

    @Override
    public String getId() {
        return "modticket-select-modal";
    }

    @Override
    public String getTitle() {
        return "Modticket öffnen";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() throws SQLException {
        List<ModalTopLevelComponent> components = new ArrayList<>();
        components.add(
                TextDisplay.of("Wähle ein oder mehrere Fachbereiche aus, die auf dieses Ticket zugreiffen können.")
        );

        // team select
        StringSelectMenu.Builder select = StringSelectMenu.create("modticket-team-select");
        select.setPlaceholder("Fachbereich auswählen");
        select.setMinValues(1).setMaxValues(TicketTeam.values().length);
        List<SelectOption> options = Arrays.stream(TicketTeam.values()).map(team -> {
            return SelectOption.of(team.getName(), team.name()).withDescription(team.getDescription());
        }).toList();
        select.addOptions(options);
        components.add(Label.of(
                "Fachbereiche",
                select.build()
        ));



        if (message != null) {
            CheckboxGroup.Builder checkboxGroupBuilder = CheckboxGroup.create("message-prove");
            checkboxGroupBuilder.addOption("Nachricht als Beweis festlegen", "use");
            checkboxGroupBuilder.addOption("Nachricht als Beweis festlegen und Löschen", "use-delete");
            checkboxGroupBuilder.setMaxValues(1);
            checkboxGroupBuilder.setRequired(false);
            components.add(Label.of("Beweislage", checkboxGroupBuilder.build()));
        }

        return components;
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        ModalMapping roleOption = Objects.requireNonNull(event.getValue("modticket-team-select"));
        List<Role> roles = roleOption.getAsStringList().stream().map(roleKey -> TicketTeam.valueOf(roleKey).getRoles()).toList().stream().flatMap(List::stream).toList();

        event.deferReply(true).queue();
        String memberId = getArgument(event.getCustomId(), "target");
        Member member = Objects.requireNonNull(event.getGuild().getMemberById(memberId));
        TicketService.getInstance().createTicket(TicketType.MODTICKET, roles, member, ThrowingConsumer.wrap(event, channel -> {
            channel.getManager().putMemberPermissionOverride(event.getMember().getIdLong(), TicketService.TEAM_PERMISSION, null).queue();
            event.getHook().editOriginal(
                    String.format("""
                            Modticket für %s wurde erstellt: %s
                            -# Darauf berechtig sind: %s
                            """, member.getAsMention(), channel.getAsMention(), roles.stream().map(Role::getAsMention).collect(Collectors.joining(", ")))
            ).setAllowedMentions(List.of()).queue();

            Ticket ticket = TicketRepository.getTicketById(channel.getIdLong());
            ticket.claim(event.getMember());
        }));
    }
}
