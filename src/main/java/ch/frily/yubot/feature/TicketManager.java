package ch.frily.yubot.feature;

import ch.frily.yubot.database.Database;
import ch.frily.yubot.embed.TicketOpenEmbed;
import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.interaction.button.btn.TicketCloseRequestBtn;
import ch.frily.yubot.interaction.button.btn.TicketPanelAwarenessBtn;
import ch.frily.yubot.interaction.button.btn.TicketPanelSupportBtn;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

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

    private List<Permission> ownerPermissions = List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);

    public static TicketManager getInstance() {
        if (instance == null) {
            instance = new TicketManager();
        }
        return instance;
    }

    public void createTicket(TicketType type, Member ticketOwner, Consumer<TextChannel> onCreated) {
        Ticket ticket = new Ticket(ticketOwner, type);

        Category ticketCategory = EnvResolver.getCategoryById(EnvKey.CATEGORY_TICKETS);
        ActionRow actionrow = ActionRow.of(List.of(
                new TicketCloseRequestBtn().build()
        ));
        TicketOpenEmbed embed = new TicketOpenEmbed();
        embed.setTicket(ticket);
        // Ticket settings
        ticketCategory.createTextChannel(generateTicketName(type, ticketOwner))
                .addMemberPermissionOverride(ticketOwner.getIdLong(), ownerPermissions, null)
                .setTopic(type.getLabel())
                .queue(textChannel -> {
                    // Ticket content
                    ticket.setChannel(textChannel);
                    textChannel.sendMessage(ticketOwner.getAsMention() + " - " + type.getResponsibleRoles().stream().map(Role::getAsMention).collect(Collectors.joining(", ")))
                            .addEmbeds(embed.build()).setComponents(actionrow).queue( message -> {
                                ticket.setWelcomeMessageId(message.getIdLong());
                                TicketRepository.createTicket(ticket);
                            });

                    onCreated.accept(textChannel);
                });
    }

    public String generateTicketName(TicketType type, Member owner) {
        String status = TicketStatus.NEW.getIcon();
        String typeId = type.getId();
        String username = owner.getUser().getEffectiveName();
        int randInt = ThreadLocalRandom.current().nextInt(100000, 999999);
        return status + typeId + "-" + username  + "-" + randInt;
    }

    /**
     * Checks if the {@link TextChannel} is a Ticket or nor
     * @return True if its a Ticketchannel / False if not
     */
    public boolean isTicketchannel(TextChannel channel) {
        TicketRepository.getTicketById(channel.getIdLong());
        return true;
    }

    /**
     * Checks if the user is a Teammember or nor
     * @param user
     * @return True if its a Teammember / False if not
     */
    public static boolean userIsTeammember(User user) {
        Role teamRole = EnvResolver.getRoleById(EnvKey.ROLE_YUTEAM);
        return EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(user.getId()).getRoles().contains(teamRole);
    }

    public IButton getButtonByTypeGroup(TicketTypeGroup typeGroup){
        return switch (typeGroup) {
            case SUPPORT -> new TicketPanelSupportBtn();
            case AWARENESS -> new TicketPanelAwarenessBtn();
        };
    }

}
