package ch.frily.yubot.service.ticket.modal;

import ch.frily.yubot.database.repository.TicketRepository;
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
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jspecify.annotations.NonNull;
import org.slf4j.ILoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
public class TicketAddUsersModal extends Modal {

    @Setter
    Ticket ticket;

    @Override
    public String getId() {
        return "add-users-modal";
    }

    @Override
    public String getTitle() {
        return "Benutzer*innen hinzufügen";
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
        usersSelect.setRequired(false);

        TextInput.Builder reasonInput = TextInput.create("reason", TextInputStyle.PARAGRAPH);
        reasonInput.setRequiredRange(2, 100);
        reasonInput.setRequired(false);

        return List.of(
                TextDisplay.of("Füge dem Ticket neue Benutzer*innen hinzu oder entferne bereits erlaubte Benutzende."),
                Label.of(
                        "Erlaubte Benutzer*innen auswählen",
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

        List<Member> currentAllowedMembers = ticket.getChannel().getMemberPermissionOverrides().stream().map(PermissionOverride::getMember).toList();
        List<Member> membersToRemove = currentAllowedMembers.stream().filter(member -> !memberIds.contains(member.getId())).toList(); // removes members that are no longer selected

        List<Member> membersToAdd = allSelectedMembers.stream().filter(selectedMember -> !currentAllowedMembers.contains(selectedMember)).toList();

        ticket.addMemberAccess(event.getMember(), membersToAdd);
        ticket.removePermissionHolder(event.getMember(), membersToRemove);

        StringBuilder reply = new StringBuilder();
        if (!membersToAdd.isEmpty()) {
            reply.append(String.format("➡️ %s wurde%s dem Ticket hinzugefügt.\n", String.join(", ", membersToAdd.stream().map(Member::getAsMention).toList()), membersToAdd.size() > 1 ? "n" : ""));
        }
        if (!membersToRemove.isEmpty()) {
            reply.append(String.format("🔻 %s wurde%s vom Ticket entfernt.\n", String.join(", ", membersToRemove.stream().map(Member::getAsMention).toList()), membersToRemove.size() > 1 ? "n" : ""));
        }

        if (Objects.nonNull(reasonValue) && !reasonValue.getAsString().isEmpty()) {
            reply.append(String.format("-# Begründung: %s", reasonValue.getAsString()));
        }

        event.reply(reply.toString()).setAllowedMentions(List.of()).queue(); // todo remove mention control
    }
}
