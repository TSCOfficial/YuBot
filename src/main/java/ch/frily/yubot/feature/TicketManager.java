package ch.frily.yubot.feature;

import ch.frily.yubot.embed.TicketOpenEmbed;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.interaction.button.btn.TicketCloseRequestBtn;
import ch.frily.yubot.interaction.button.btn.TicketPanelAwarenessBtn;
import ch.frily.yubot.interaction.button.btn.TicketPanelBewerbungBtn;
import ch.frily.yubot.interaction.button.btn.TicketPanelSupportBtn;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
/**
 * The {@link TicketManager} manages the tickets, such as creating or checking permissions
 */
public class TicketManager {

    private static TicketManager instance;

    /** Permissions for the ticketowner and supportteam */
    protected static final List<Permission> USER_PERMISSION = List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES);

    /** Maximum ticket count per Person over all categories */
    private static final int MAX_TICKET_COUNT = 5;

    public static TicketManager getInstance() {
        if (instance == null) {
            instance = new TicketManager();
        }
        return instance;
    }

    public void createTicket(TicketType type, Member ticketOwner, Consumer<TextChannel> onCreated) throws SQLException, ClassNotFoundException {
        List<TextChannel> openedTickets = TicketRepository.getTicketsByMember(ticketOwner).stream().map(Ticket::getChannel).toList();
        if (openedTickets.size() >= MAX_TICKET_COUNT) {
            throw new PermissionDeniedException(String.format("""
                    Du hast bereits zu viele Tickets offen *(%d)*!
                    
                    Verwende möglichst deine bereits geöffneten Tickets:
                    - %s
                    """, openedTickets.size(), openedTickets.stream().map(Channel::getAsMention).collect(Collectors.joining("\n- "))
            ), "Falls dein aktuelles Anliegen zu keinem deiner offenen Tickets passt, melde dich bitte beim Moderations-Team."
            );
        }
        if (TicketTypeControlRepository.isTypeLocked(type)) {
            throw new PermissionDeniedException(
                    String.format("Die Ticketart **%s** ist gesperrt!", type.getLabel()),
                    "Vielen Dank für dein Interesse! Versuche es gerne ein ander mal wieder."
            );
        }

        Ticket ticket = new Ticket(ticketOwner, type);

        Category ticketCategory = EnvResolver.getCategoryById(EnvKey.CATEGORY_TICKETS);
        ActionRow actionrow = ActionRow.of(List.of(
                new TicketCloseRequestBtn().build()
        ));
        TicketOpenEmbed embed = new TicketOpenEmbed();
        embed.setTicket(ticket);
        // Ticket settings
        ticketCategory.createTextChannel(generateTicketName(type, ticketOwner))
                .addMemberPermissionOverride(ticketOwner.getIdLong(), USER_PERMISSION, null)
                .setTopic(type.getLabel())
                .queue(textChannel -> {
                    // Ticket team permissions
                    TextChannelManager ticketManager = textChannel.getManager();
                    ticket.getType().getResponsibleRoles().forEach(role -> {
                        ticketManager.putRolePermissionOverride(role.getIdLong(), USER_PERMISSION, null);
                    });
                    ticketManager.queue();


                    // Ticket content
                    ticket.setChannel(textChannel);
                    textChannel.sendMessage(ticketOwner.getAsMention() + " - " + type.getResponsibleRoles().stream().map(Role::getAsMention).collect(Collectors.joining(", ")))
                            .addEmbeds(embed.build()).setComponents(actionrow).queue(ThrowingConsumer.wrap(null, message -> {
                                ticket.setWelcomeMessageId(message.getIdLong());
                                TicketRepository.createTicket(ticket);

                                onCreated.accept(textChannel); // reply at the very end, to let everything finish first
                            }));
                });
    }

    public String generateTicketName(TicketType type, Member owner) {
        String status = TicketStatus.NEW.getIcon();
        String typeId = type.getId();
        String username = owner.getUser().getEffectiveName().substring(0, 26); // take only 25 chars of user's name
        int randInt = ThreadLocalRandom.current().nextInt(100000, 999999);
        return status + typeId + "-" + username  + "-" + randInt;
    }

    /**
     * Checks if the {@link TextChannel} is a Ticket or nor
     * @return True if its a Ticketchannel / False if not
     */
    public boolean isTicketchannel(TextChannel channel) throws SQLException, ClassNotFoundException {
        if (channel.getParentCategory() == null || channel.getParentCategory().getIdLong() != EnvResolver.getCategoryById(EnvKey.CATEGORY_TICKETS).getIdLong()) {
            return false;
        }
        TicketRepository.getTicketById(channel.getIdLong());
        return true;
    }

    public IButton getButtonByTypeGroup(TicketTypeGroup typeGroup){
        return switch (typeGroup) {
            case SUPPORT -> new TicketPanelSupportBtn();
            case AWARENESS -> new TicketPanelAwarenessBtn();
            case BEWERBUNG -> new TicketPanelBewerbungBtn();
        };
    }

}
