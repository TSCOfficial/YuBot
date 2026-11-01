package ch.frily.yubot.service.ticket.modal;

import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.TicketTeam;
import ch.frily.yubot.service.ticket.TicketType;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class TicketAccessTeamsModal extends Modal {

    @Setter
    Ticket ticket;

    @Override
    public String getId() {
        return "access-teams-modal";
    }

    @Override
    public String getTitle() {
        return "Zugriff für Fachbereiche bearbeiten";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() throws SQLException {
        StringSelectMenu.Builder teamsSelect = StringSelectMenu.create("teams-select");
        List<Role> alreadyAllowedRoles = ticket.getChannel().getRolePermissionOverrides().stream().map(PermissionOverride::getRole).toList();
        List<TicketTeam> alreadyAllowedTeams = TicketTeam.getTeamsByRoles(alreadyAllowedRoles);
        log.info("Already allowed teams: {}", alreadyAllowedTeams.stream().map(TicketTeam::name).toList());
        alreadyAllowedTeams = alreadyAllowedTeams.stream().limit(26).toList();

        teamsSelect.addOptions(Arrays.stream(TicketTeam.values()).map(team -> {
            return SelectOption.of(team.getName(), team.name()).withDescription(team.getDescription());
        }).toList());

        teamsSelect.setDefaultOptions(alreadyAllowedTeams.stream().map(team -> {
            return SelectOption.of(team.getName(), team.name()).withDescription(team.getDescription());
        }).toList());

        teamsSelect.setMinValues(0);
        teamsSelect.setMaxValues(25);
        teamsSelect.setRequired(false);
        teamsSelect.setPlaceholder("Noch keine Teams ausgewählt");

        TextInput.Builder reasonInput = TextInput.create("reason", TextInputStyle.PARAGRAPH);
        reasonInput.setRequiredRange(2, 100);
        reasonInput.setRequired(false);

        return List.of(
                TextDisplay.of("Füge dem Ticket neue Teams hinzu oder entferne bereits erlaubte Teams."),
                Label.of(
                        "Erlaubte Teams auswählen",
                        "Hinzugefügte Teams werden über ihr neuen Ticketzugang nicht Benachrichtigt.",
                        teamsSelect.build()
                ),
                Label.of(
                        "Begründung",
                        reasonInput.build()
                )
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        ModalMapping teamsValue = event.getValue("teams-select");
        ModalMapping reasonValue = event.getValue("reason");
        List<String> teamNames = teamsValue.getAsStringList();
        List<TicketTeam> allSelectedTeams = Stream.of(TicketTeam.values()).filter(team -> teamNames.contains(team.name())).toList();

        Ticket ticket = TicketRepository.getTicketByChannelId(event.getChannelIdLong());

        List<Role> currentAllowedRoles = ticket.getChannel().getRolePermissionOverrides().stream().map(PermissionOverride::getRole).toList();
        List<TicketTeam> currentlyAllowedTeams = TicketTeam.getTeamsByRoles(currentAllowedRoles);

        List<TicketTeam> teamsToRemove = currentlyAllowedTeams.stream().filter(team -> !teamNames.contains(team.name())).toList(); // removes teams that are no longer selected
        List<TicketTeam> teamsToAdd = allSelectedTeams.stream().filter(team -> !currentlyAllowedTeams.contains(team)).toList();

        List<Role> allRolesToAdd = new ArrayList<>();
        teamsToAdd.forEach(team -> {
            allRolesToAdd.addAll(team.getRoles());
        });

        List<Role> allRolesToRemove = new ArrayList<>();
        teamsToRemove.forEach(team -> {
            allRolesToRemove.addAll(team.getRoles());
        });

        TextChannelManager ticketManager = ticket.getChannel().getManager();
        ticket.addRoleAccess(ticketManager, allRolesToAdd);
        ticket.removePermissionHolder(ticketManager, allRolesToRemove);
        ticketManager.queue();

        StringBuilder reply = new StringBuilder();
        if (!teamsToAdd.isEmpty()) {
            reply.append(String.format("➡️ %s wurde%s dem Ticket hinzugefügt.\n", String.join(", ", allRolesToAdd.stream().map(Role::getAsMention).toList()), teamsToAdd.size() > 1 ? "n" : ""));
        }
        if (!teamsToRemove.isEmpty()) {
            reply.append(String.format("🔻 %s wurde%s vom Ticket entfernt.\n", String.join(", ", allRolesToRemove.stream().map(Role::getAsMention).toList()), teamsToRemove.size() > 1 ? "n" : ""));
        }

        if (Objects.nonNull(reasonValue) && !reasonValue.getAsString().isEmpty()) {
            reply.append(String.format("-# Begründung: %s", reasonValue.getAsString()));
        }

        event.reply(reply.toString()).setAllowedMentions(List.of()).queue();
    }
}
