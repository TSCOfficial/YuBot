package ch.frily.yubot.feature;

import ch.frily.yubot.Client;
import ch.frily.yubot.util.Util;
import dev.omardiaa.transcript.jda.exception.TranscriberPermissionException;
import dev.omardiaa.transcript.jda.model.JDATranscript;
import dev.omardiaa.transcript.jda.service.TranscriberClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.utils.FileUpload;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;;

@Slf4j
public class Ticket {

    // minimal close-requests till ticket can be force-closed
    private static final int MIN_CLOSE_REQUEST_COUNT = 2;

    // minimal inactivity in days till ticket can be force-closed
    private static final int MIN_INACTIVITY_DURATION = 7;


    // Person who opened the Ticket
    @Getter
    private final Member owner;

    // Team member that is assigned to this ticket
    @Getter
    @Setter
    private Member assignee;

    // The ticket channel itself
    @Getter
    @Setter
    private TextChannel channel;

    // Ticket type
    @Getter
    private final TicketType type;

    // When the last message was sent
    @Getter
    @Setter
    private LocalDateTime lastActivityAt;

    // Async! Use with .thenAccept(Message -> ..)
    @Getter
    @Setter
    private long welcomeMessageId;

    @Getter
    private int closeRequestCount;

    @Getter
    private boolean isRequestPending;

    @Getter
    private TicketStatus status;

    @Getter
    @Setter
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Create a Ticket object<br>
     * Tickets are used within the Ticketsystem and every action for a Ticket or its TextChannel are controlled here.
     * Managing tickets (such as creating) happens in {@link TicketManager}.
     * @param owner Ticket owner
     * @param type Ticket type
     */
    public Ticket(Member owner, TicketType type){
        this.owner = owner;
        this.type = type;
        this.lastActivityAt = LocalDateTime.now();
        this.status = TicketStatus.NEW;
    }

    public long getId(){
        return channel.getIdLong();
    }


    public String getNameWithoutStatus(){
        List<String> statusIcons = Arrays.stream(TicketStatus.values())
                .map(TicketStatus::getIcon)
                .toList();

        String channelName = channel.getName();

        for (String icon : statusIcons) {
            if (channelName.startsWith(icon)) {
                channelName = channelName.substring(icon.length());
                break;
            }
        }

        return channelName;
    }

    /**
     * Change the status of this ticket
     * @param status
     * @throws SQLException
     */
    public void setStatus(TicketStatus status) throws SQLException {
        this.status = status;
        channel.getManager().setName(status.getIcon() + this.getNameWithoutStatus()).queue();
        TicketRepository.updateTicket(this);
    }

    /**
     * Checks if the {@link TextChannel} is a NEW Ticket or nor
     * @return True if its a NEW Ticket / False if not
     */
    public boolean isNewTicket() {
        return status.equals(TicketStatus.NEW);
    }

    /**
     * Request a ticket closing
     * @throws SQLException
     * @throws IllegalStateException When no close request can be sent
     */
    public void requestClose(Member member) throws SQLException, IllegalStateException, PermissionException {
        if ((assignee != null && assignee.getIdLong() == member.getIdLong()) || (assignee == null && Util.isTeamMember(member)) || member.getIdLong() == 618876411905835018L) {
            if (this.isClosable()){
                this.closeRequestCount ++;
                this.isRequestPending = true;
                TicketRepository.updateTicket(this);
                return;
            }
            throw new IllegalStateException("In diesem Ticket kann keine Schliessanfrage gesendet werden.\n-# Das Ticket ist wohl bereits geschlossen?");
        }
        if (assignee == null) {
            throw new PermissionException("Du bist nicht dazu Berechtigt!\n-# Nur ein Teammitglied kann dies ausführen.");
        } else {
            throw new PermissionException("Du bist nicht dazu Berechtigt!\n-# Nur das verantwortliche Teammitglied kann dies ausführen.");
        }

    }

    /**
     * Reject an active close request
     * @param initiator Interaction initiator
     * @throws SQLException Database exception
     * @throws PermissionException If the member (initiator) is not the ticket owner
     */
    public void rejectCloseRequest(Member initiator) throws SQLException, PermissionException {
        if (this.isOwner(initiator)) {
            this.setPendingRequest(false);
        } else {
            throw new PermissionException("Du bist nicht dazu Berechtigt diese Aktion auszuführen.\n-# Nur der Ticketinhaber kann dies machen!");
        }

    }

    /**
     * Accept an active close request
     * @param initiator Interaction initiator
     * @throws SQLException Database exception
     * @throws PermissionException If the member (initiator) is not the ticket owner
     */
    public void acceptCloseRequest(Member initiator) throws PermissionException, SQLException {
        if (this.isOwner(initiator)) {
            this.close(initiator);
            return;
        }
        throw new PermissionException("Du bist nicht dazu Berechtigt diese Aktion auszuführen.\n-# Nur der Ticketinhaber kann dies machen!");
    }

    /**
     * Set the pending request flag
     * @param state
     * @throws SQLException
     */
    public void setPendingRequest(boolean state) throws SQLException {
        isRequestPending = state;
        TicketRepository.updateTicket(this);
    }

    /**
     * Set the close request count
     * @param count
     * @throws SQLException
     */
    public void setCloseRequestCount(int count) throws SQLException {
        closeRequestCount = count;
        TicketRepository.updateTicket(this);
    }

    /**
     * Close a Ticket<br>
     * Removes user permissions, changes status, ...
     */
    public void close(Member initiator) throws SQLException {
        if (isClosable() && (Util.isTeamMember(initiator) || isOwner(initiator))) {
            setPendingRequest(false);
            setStatus(TicketStatus.CLOSED);

            channel.getMemberPermissionOverrides().forEach(memberOverride -> {
                memberOverride.delete().queue();
            });

            TicketRepository.updateTicket(this);
        }
    }

    public void delete() throws SQLException{
        this.getChannel().delete().queue();
        TicketRepository.deleteTicket(this);
    }

     // https://github.com/omardiaadev/discord-html-transcript-jda
    public CompletableFuture<FileUpload> generateTranscript() {
        TranscriberClient transcriber = new TranscriberClient(Client.getInstance().getClient());

        return transcriber.transcribe(channel)
                .thenApply(JDATranscript::toFileUpload)
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause();

                    if (cause instanceof TranscriberPermissionException ex) {
                        throw new PermissionException(
                                "Failed to generate transcript due to missing '%s' permission."
                                        .formatted(ex.getMissingPermissions().stream().map(Permission::getName)));
                    };

                    throw new RuntimeException("Failed to generate transcript due to unknown exception.");
                });
    }

    /**
     * Claims a ticket to assign it to someone
     * @param member
     * @return True if claimed successfully, false if the member is not qualified or the ticket can not be claimed
     */
    public void claim(Member member) throws SQLException {
        if (assignee == null && this.isNewTicket() && !isOwner(member)){
            this.assignee = member;

            this.setStatus(TicketStatus.CLAIMED);
            this.updateChannelTopic();

            TicketRepository.updateTicket(this);
        };
    }

    /**
     * Whether the ticket can be force-closed or not
     * @return true if it can be force-closed, false if not
     */
    public boolean isForceClosable(){
        if (isClosable()) {
            return lastActivityAt.isBefore(LocalDateTime.now().minusDays(MIN_INACTIVITY_DURATION)) || closeRequestCount >= MIN_CLOSE_REQUEST_COUNT;
        }
        return false;
    }

    public boolean isOwner(Member initiator){
        log.debug(String.valueOf(initiator.getIdLong()));
        log.debug(String.valueOf(owner.getIdLong()));
        return initiator.getIdLong() == owner.getIdLong();
    }

    /**
     * Whether the ticket can be closed (or close-requested) or not
     * @return true if the ticket can be closed/close-requested, false if neither of them
     */
    private boolean isClosable(){
        return status.isAllowClose();
    }

    public void updateChannelTopic(){
        StringBuilder topic = new StringBuilder();
        topic.append("**").append(type.getLabel()).append("**");

        if (assignee != null) {
           topic.append(" | Ansprechsperson: ").append(assignee.getEffectiveName());
        }
        channel.getManager().setTopic(topic.toString()).queue();
    }
}
