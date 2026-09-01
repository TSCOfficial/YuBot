package ch.frily.yubot.interaction.button.btn.ticket;

import ch.frily.yubot.container.ticket.TicketTranscriptContainer;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.feature.ticket.TicketRepository;
import ch.frily.yubot.feature.ticket.TicketTypeGroup;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.modal.modal.TicketSummaryModal;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class TicketDeleteBtn extends Button {
    @Override
    public String getId() {
        return "ticket-delete-btn";
    }

    @Override
    public String getLabel() {
        return "Löschen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public EmojiUnion getEmoji() {
        return Emoji.fromFormatted("🗑️");
    }

    @Override
    public void execute(@NotNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException {

        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());

        if (ticket.getType().getGroup() == TicketTypeGroup.BEWERBUNG) {
            ticket.generateTranscript().thenAccept(ThrowingConsumer.wrap(event, fileUpload -> {
                fileUpload.setName("transkript-" + ticket.getNameWithoutStatus() + ".html");
                TextChannel logChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_TICKETLOGS);
                List<Container> containers = new TicketTranscriptContainer(event.getMember(), ticket, fileUpload, null).build();
                logChannel.sendMessageComponents(containers).useComponentsV2().addFiles(fileUpload).setAllowedMentions(List.of()).queue(ThrowingConsumer.wrap(event, message -> {
                    event.getHook().editOriginal("Ticket wird gelöscht.").queue();
                    ticket.delete();
                }));
            }));
            return;
        }

        TicketSummaryModal summaryModal = new TicketSummaryModal();
        summaryModal.setTicket(ticket);
        event.replyModal(summaryModal.build()).queue();
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_YUTEAM
        ).map(EnvResolver::getRoleById).toList();
    }
}
