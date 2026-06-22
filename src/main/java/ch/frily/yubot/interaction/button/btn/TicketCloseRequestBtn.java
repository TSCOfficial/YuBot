package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.embed.TicketCloseRequestEmbed;
import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.interaction.button.IButton;
import javassist.NotFoundException;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class TicketCloseRequestBtn implements IButton {
    @Override
    public String getId() {
        return "ticket-close-request-btn";
    }

    @Override
    public String getLabel() {
        return "Schliessen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.DANGER;
    }

    @Override
    public EmojiUnion getEmoji() {
        return Emoji.fromFormatted("🔒");
    }

    @Override
    public void execute(@NotNull ButtonInteractionEvent event) throws SQLException, IllegalStateException, ClassNotFoundException {
        Ticket ticket = TicketRepository.getTicketById(event.getChannelIdLong());
        ticket.requestClose(event.getMember(), false);

        ActionRow actionRow = ActionRow.of(
                new TicketCloseRequestAcceptBtn().build(),
                new TicketCloseRequestRejectBtn().build()
        );

        TicketCloseRequestEmbed optionsEmbed = new TicketCloseRequestEmbed();
        optionsEmbed.setInitiator(event.getMember());
        optionsEmbed.setTicket(ticket);

        event.reply(ticket.getOwner().getAsMention()).addEmbeds(optionsEmbed.build()).setComponents(actionRow).queue();

        event.getMessage().editMessageComponents(event.getMessage().getComponentTree().asDisabled()).queue();
    }
}
