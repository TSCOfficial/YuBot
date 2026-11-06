package ch.frily.yubot.service.ticket.modal;

import ch.frily.yubot.service.ticket.repository.TicketRepository;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.util.EnvKey;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class TicketAccessUsersModal extends Modal {

    @Setter
    Ticket ticket;

    @Override
    public String getId() {
        return "access-users-modal";
    }

    @Override
    public String getTitle() {
        return "Zugriff für Benutzer*innen bearbeiten";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() throws SQLException {
        EntitySelectMenu.Builder usersSelect = EntitySelectMenu.create("users-select", EntitySelectMenu.SelectTarget.USER);
        List<EntitySelectMenu.DefaultValue> alreadyAllowedMembers = ticket.getChannel().getMemberPermissionOverrides().stream().map(override -> {
            Member member = override.getMember();
            return EntitySelectMenu.DefaultValue.from(member);
        }).toList();
        alreadyAllowedMembers = alreadyAllowedMembers.stream().limit(26).toList();
        usersSelect.setDefaultValues(alreadyAllowedMembers);
        usersSelect.setMinValues(0);
        usersSelect.setMaxValues(25);
        usersSelect.setPlaceholder("Noch keine Benutzer*innen ausgewählt");
        usersSelect.setRequired(false);

        TextInput.Builder reasonInput = TextInput.create("reason", TextInputStyle.PARAGRAPH);
        reasonInput.setRequiredRange(2, 100);
        reasonInput.setRequired(false);

        return List.of(
                TextDisplay.of("Füge dem Ticket neue Benutzer*innen hinzu oder entferne bereits erlaubte Benutzende."),
                Label.of(
                        "Erlaubte Benutzer*innen auswählen",
                        "Hinzugefügte Personen werden über ihr neuen Ticketzugang Benachrichtigt.",
                        usersSelect.build()
                ),
                Label.of(
                        "Begründung",
                        reasonInput.build()
                )
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        ModalMapping usersValue = event.getValue("users-select");
        ModalMapping reasonValue = event.getValue("reason");
        List<String> memberIds = usersValue.getAsStringList();
        List<Member> allSelectedMembers = memberIds.stream().map(memberId -> EnvKey.GUILD_YUSERVER.getAsGuild().getMemberById(memberId)).toList(); // depending whether they are already added or not, will be filtered out to lower API calls

        Ticket ticket = TicketRepository.getTicketByChannelId(event.getChannelIdLong());
        List<String> uncompletedAction = new ArrayList<>();

        List<Member> currentAllowedMembers = ticket.getChannel().getMemberPermissionOverrides().stream().map(PermissionOverride::getMember).toList();
        List<Member> membersToRemove = currentAllowedMembers.stream().filter(member -> {
            if (member.equals(event.getMember())) {
                uncompletedAction.add(String.format("%s konnte nicht entfernt werden, da du dich selbst nicht Entfernen kannst.", member.getAsMention()));
                return false;
            }
            if (member.equals(ticket.getMemberOwner())) {
                uncompletedAction.add(String.format("%s konnte nicht entfernt werden, da der/die Besitzer*in des Tickets nicht Entfernen werden kann.", member.getAsMention()));
                return false;
            }
            return !memberIds.contains(member.getId());
        }).toList(); // removes members that are no longer selected

        List<Member> membersToAdd = allSelectedMembers.stream().filter(selectedMember -> !currentAllowedMembers.contains(selectedMember)).toList();

        TextChannelManager ticketManager = ticket.getChannel().getManager();
        ticket.addMemberAccess(ticketManager, membersToAdd);
        ticket.removePermissionHolder(ticketManager, membersToRemove);
        ticketManager.queue();

        StringBuilder reply = new StringBuilder();
        if (!membersToAdd.isEmpty()) {
            reply.append(String.format("➡️ %s wurde%s dem Ticket hinzugefügt.\n", String.join(", ", membersToAdd.stream().map(Member::getAsMention).toList()), membersToAdd.size() > 1 ? "n" : ""));
        }
        if (!membersToRemove.isEmpty()) {
            reply.append(String.format("🔻 %s wurde%s vom Ticket entfernt.\n", String.join(", ", membersToRemove.stream().map(Member::getAsMention).toList()), membersToRemove.size() > 1 ? "n" : ""));
        }
        if (!uncompletedAction.isEmpty()) {
            reply.append(String.format("-# ⚠️ %s", String.join("\n-# ⚠️ ", uncompletedAction)));
        }

        if (Objects.nonNull(reasonValue) && !reasonValue.getAsString().isEmpty()) {
            reply.append(String.format("-# Begründung: %s", reasonValue.getAsString()));
        }

        event.reply(reply.toString()).queue();
    }
}
