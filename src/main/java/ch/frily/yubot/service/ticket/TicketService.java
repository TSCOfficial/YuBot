package ch.frily.yubot.service.ticket;

import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.database.repository.TicketTypeControlRepository;
import ch.frily.yubot.embed.ticket.TicketOpenEmbed;
import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.service.Service;
import ch.frily.yubot.service.ticket.button.*;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.service.ticket.ctxmenu.ModticketMsgCtxMenu;
import ch.frily.yubot.service.ticket.ctxmenu.ModticketUserCtxMenu;
import ch.frily.yubot.service.ticket.event.TicketMessageReceivedEvent;
import ch.frily.yubot.service.ticket.event.TicketOwnerLeftGuildEvent;
import ch.frily.yubot.service.ticket.modal.ModTicketSelectorModal;
import ch.frily.yubot.service.ticket.slashcommand.TicketCmdGroup;
import ch.frily.yubot.interaction.contextmenu.IContextMenu;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.ticket.modal.TicketSummaryModal;
import ch.frily.yubot.service.ticket.modal.TicketTypeSelectorModal;
import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.service.ticket.schedule.TicketActivityScheduler;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TicketService extends Service {

    private static TicketService instance;

    /** Permissions for the ticketowner and supportteam */
    public static final List<Permission> USER_PERMISSION = List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.USE_APPLICATION_COMMANDS);
    public static final List<Permission> TEAM_PERMISSION = List.of(Permission.MESSAGE_MENTION_EVERYONE, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.USE_APPLICATION_COMMANDS);

    /** Maximum ticket count per Person over all categories */
    private static final int MAX_TICKET_COUNT = 5;

    public static TicketService getInstance() {
        if (instance == null) {
            instance = new TicketService();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Ticketsystem";
    }

    @Override
    public String getDescription() {
        return "Ermöglichst ein geschützten Kommunikationskanal mit dem Serverteam. Zwei-Stufen-Ticketsystem.";
    }

    @Override
    public List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of(
                new TicketCmdGroup()
        );
    }

    @Override
    public List<Button> getButtons() {
        return List.of(
                new TicketCancelOpenBtn(),
                new TicketCloseRequestAcceptBtn(),
                new TicketCloseRequestBtn(),
                new TicketCloseRequestRejectBtn(),
                new TicketConfirmOpenBtn(),
                new TicketDeleteBtn(),
                new TicketPanelSupportBtn(),
                new TicketPanelAwarenessBtn(),
                new TicketPanelBewerbungBtn()
        );
    }

    @Override
    public List<Modal> getModals() {
        return List.of(
                new TicketSummaryModal(),
                new TicketTypeSelectorModal(),
                new ModTicketSelectorModal()
        );
    }

    @Override
    public List<IScheduler> getSchedulers() {
        return List.of(
                new TicketActivityScheduler()
        );
    }

    @Override
    public List<IContextMenu> getContextMenus() {
        return List.of(
                new ModticketUserCtxMenu(),
                new ModticketMsgCtxMenu()
        );
    }

    @Override
    public List<IEvent<? extends Event>> getEvents() {
        return List.of(
                TicketOwnerLeftGuildEvent.getInstance(),
                TicketMessageReceivedEvent.getInstance()
        );
    }

    public void createTicket(TicketType type, Member ticketOwner, Consumer<TextChannel> onCreated) throws SQLException {
        createTicket(type, type.getResponsibleRoles(), ticketOwner, onCreated);
    }
    public void createTicket(TicketType type, List<Role> responsibleRoles, Member ticketOwner, Consumer<TextChannel> onCreated) throws SQLException {
        List<TextChannel> openedTickets = TicketRepository.getTicketsByUser(ticketOwner.getUser()).stream().map(Ticket::getChannel).toList();
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

        Category ticketCategory = type.getGroup() != null ? type.getGroup().getChannelCategory() : EnvKey.CATEGORY_TICKETSUPPORT.getAsCategory();
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
                    responsibleRoles.forEach(role -> {
                        ticketManager.putRolePermissionOverride(role.getIdLong(), TEAM_PERMISSION, null);
                    });
                    ticketManager.queue();


                    // Ticket content
                    ticket.setChannel(textChannel);
                    String roleMentions = " - " + responsibleRoles.stream().map(Role::getAsMention).collect(Collectors.joining(", "));
                    if (ticket.getType() == TicketType.MODTICKET) {
                        roleMentions = "";
                    }
                    textChannel.sendMessage(ticketOwner.getAsMention() + roleMentions)
                            .addEmbeds(embed.build()).setComponents(actionrow).queue(ThrowingConsumer.wrap(null, message -> {
                                textChannel.pinMessageById(message.getIdLong()).queue(); // todo delete message, add feature after the 30.10.2026
                                ticket.setWelcomeMessageId(message.getIdLong());
                                TicketRepository.createTicket(ticket);

                                onCreated.accept(textChannel); // reply at the very end, to let everything finish first
                            }));
                });
    }

    public String generateTicketName(TicketType type, Member owner) {
        String status = TicketStatus.NEW.getIcon();
        String typeId = type.getId();
        String username = owner.getUser().getEffectiveName();
        if (owner.getUser().getEffectiveName().length() > 26) {
            username = owner.getUser().getEffectiveName().substring(0, 26); // take only 25 chars of user's name
        }

        int randInt = ThreadLocalRandom.current().nextInt(100000, 999999);
        return status + typeId + "-" + username  + "-" + randInt;
    }

    /**
     * Checks if the {@link TextChannel} is a Ticket or nor
     * @return True if its a Ticketchannel / False if not
     */
    public boolean isTicketchannel(TextChannel channel) throws SQLException {
        if (channel.getParentCategory() == null || channel.getParentCategory().getIdLong() != EnvKey.CATEGORY_TICKETSUPPORT.getAsCategory().getIdLong()) {
            return false;
        }
        TicketRepository.getTicketById(channel.getIdLong());
        return true;
    }

    public Button getButtonByTypeGroup(TicketTypeGroup typeGroup){
        return switch (typeGroup) {
            case SUPPORT -> new TicketPanelSupportBtn();
            case AWARENESS -> new TicketPanelAwarenessBtn();
            case BEWERBUNG -> new TicketPanelBewerbungBtn();
        };
    }
}
