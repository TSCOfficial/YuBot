package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.container.TicketTranscriptContainer;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.HandledException;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import javassist.NotFoundException;
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
public class TicketDeleteBtn implements IButton {
    @Override
    public String defineId() {
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
        ticket.generateTranscript().thenAccept(ThrowingConsumer.wrap(event, fileUpload -> {
            fileUpload.setName("transkript-" + ticket.getNameWithoutStatus() + ".html");
            TextChannel logChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_TICKETLOGS);
            List<Container> containers = new TicketTranscriptContainer(event.getMember(), ticket, fileUpload).build();
            logChannel.sendMessageComponents(containers).useComponentsV2().addFiles(fileUpload).setAllowedMentions(List.of()).queue();

            ticket.delete();

        }));
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_YUTEAM
        ).map(EnvResolver::getRoleById).toList();
    }
}
