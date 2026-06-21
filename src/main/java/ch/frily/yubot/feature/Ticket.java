package ch.frily.yubot.feature;

import ch.frily.yubot.Client;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import dev.omardiaa.transcript.jda.exception.TranscriberPermissionException;
import dev.omardiaa.transcript.jda.model.JDATranscript;
import dev.omardiaa.transcript.jda.service.TranscriberClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.utils.FileUpload;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;;

@Slf4j
public class Ticket {

    // minimal close-requests till ticket can be force-closed
    private static final int MIN_CLOSE_REQUEST_COUNT = 2;

    // minimal inactivity of the ticket-owner till ticket can be force-closed [in days]
    private static final int MIN_INACTIVITY_DURATION = 3;


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

    private static final List<Role> ALLOW_DIRECT_FORCECLOSE_ROLES = Stream.of(
            EnvKey.ROLE_ADMIN,
            EnvKey.ROLE_SERVERLEITUNG,
            EnvKey.ROLE_MODLEITUNG
    ).map(EnvResolver::getRoleById).toList();

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
     */
    public void setStatus(TicketStatus status) {
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
     * @throws IllegalStateException When no close request can be sent
     * @throws PermissionDeniedException When the member is not allowed to close the ticket
     */
    public void requestClose(Member initiator, boolean forceClose) throws IllegalStateException, PermissionDeniedException {
        if (Util.isTeamMember(initiator)) {
            if (!isOwner(initiator)) {
                if (this.isClosable()) {
                    if (forceClose) {
                        forceClose(initiator);
                    } else {
                        this.closeRequestCount++;
                        this.isRequestPending = true;
                        TicketRepository.updateTicket(this);
                    }
                    return;
                }

                throw new IllegalStateException("In diesem Ticket kann keine Schliessanfrage gesendet werden.\n-# Das Ticket ist wohl bereits geschlossen?");
            }
            throw new PermissionDeniedException("Du, als Ticket-ersteller*in, kannst das Ticket nicht selbst schliessen.");
        }
        throw new PermissionDeniedException("Nur ein Teammitglied kann diese Aktion ausführen.");
    }

    /**
     * Reject an active close request
     * @param initiator Interaction initiator
     * @throws PermissionDeniedException If the member (initiator) is not the ticket owner
     */
    public void rejectCloseRequest(Member initiator) throws PermissionDeniedException {
        if (this.isOwner(initiator)) {
            this.setPendingRequest(false);
        } else {
            throw new PermissionDeniedException(String.format("Nur der/die Ticketinhaber\\*in %s kann dies machen!", owner.getAsMention()));
        }

    }

    /**
     * Accept an active close request
     * @param initiator Interaction initiator
     * @throws PermissionDeniedException If the member (initiator) is not the ticket owner
     */
    public void acceptCloseRequest(Member initiator) throws PermissionDeniedException{
        if (this.isOwner(initiator)) {
            this.close(initiator);
            return;
        }
        throw new PermissionDeniedException(String.format("Nur der/die Ticketinhaber\\*in %s kann dies machen!", owner.getAsMention()));
    }

    /**
     * Set the pending request flag
     * @param state
     */
    public void setPendingRequest(boolean state) {
        isRequestPending = state;
        TicketRepository.updateTicket(this);
    }

    /**
     * Set the close request count
     * @param count
     */
    public void setCloseRequestCount(int count) {
        closeRequestCount = count;
        TicketRepository.updateTicket(this);
    }

    public void forceClose(Member initiator) throws PermissionDeniedException {
        if (Util.isTeamMember(initiator)) {
            if (!isOwner(initiator)) {
                if (ALLOW_DIRECT_FORCECLOSE_ROLES.stream().anyMatch(role -> initiator.getRoles().contains(role))) {
                    close(initiator);
                    return;
                } else if (isForceClosable()) {
                    close(initiator);
                    return;
                } else {
                    throw new InvalidStateException(
                            "Ticket kann nicht geschlossen werden.",
                            String.format("Es müssen erst min. %d Anfragen gestellt werden oder %d Tage inaktivität.", MIN_CLOSE_REQUEST_COUNT, MIN_INACTIVITY_DURATION)
                    );
                }
            }


        }


    }

    /**
     * Close a Ticket<br>
     * Removes user permissions, changes status, ...
     */
    public void close(Member initiator) {
        if (isClosable() && (Util.isTeamMember(initiator) || isOwner(initiator))) {
            setPendingRequest(false);
            setStatus(TicketStatus.CLOSED);

            channel.getMemberPermissionOverrides().forEach(memberOverride -> {
                memberOverride.delete().queue();
            });

            TicketRepository.updateTicket(this);
        }
    }

    public void delete() {
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
    public void claim(Member member) {
        if (assignee == null && this.isNewTicket() && !isOwner(member)){
            this.assignee = member;

            this.setStatus(TicketStatus.CLAIMED);
            this.updateChannelTopic();

            TicketRepository.updateTicket(this);
        };
    }

    /**
     * Add a member to this ticket
     * @param initiator
     * @param member
     * @throws PermissionDeniedException
     * @throws InvalidStateException
     */
    public void addMember(Member initiator, Member member) throws PermissionDeniedException, InvalidStateException {
        toggleAdditionalMember(initiator, member, true);
    }

    /**
     * Remove member from this ticket
     * @param initiator
     * @param member
     * @throws PermissionDeniedException
     * @throws InvalidStateException
     */
    public void removeMember(Member initiator, Member member) throws PermissionDeniedException, InvalidStateException {
        toggleAdditionalMember(initiator, member, false);
    }

    public void toggleAdditionalMember(Member initiator, Member member, boolean addMember) throws PermissionDeniedException, InvalidStateException {
        if (!isOwner(initiator) && Util.isTeamMember(initiator)){
            if (status != TicketStatus.CLOSED) {

                if (addMember) {
                    if (getMemberPermissionOverrides().contains(member)) {
                        throw new InvalidStateException("Diese Person kann nicht hinzugefügt werden.", "Diese befindet sich bereits in diesem Ticket.");
                    }
                    channel.getManager().putPermissionOverride(member, TicketManager.USER_PERMISSION, List.of()).queue();
                } else {
                    log.debug("Removing member from ticket");
                    if (!getMemberPermissionOverrides().contains(member)) {
                        throw new InvalidStateException("Diese Person kann nicht entfernt werden.", "Diese befindet sich nicht im Ticket.");
                    }
                    if (member == owner) {
                        throw new InvalidStateException("Diese Person kann nicht entfernt werden.", "Der/Die Ticketinhaber\\*in kann nicht entfernt werden.");
                    }
                    channel.getManager().removePermissionOverride(member).queue();
                }
                return;
            }
            throw new InvalidStateException("Das Ticket ist geschlossen.", "Es kann keine Person hinzugefügt werden, wenn das Ticket geschlossen ist.");
        }
        throw new PermissionDeniedException("Nur ein Teammitglied kann diese Aktion ausführen");
    }

    private List<Member> getMemberPermissionOverrides() {
        return channel.getMemberPermissionOverrides().stream().map(PermissionOverride::getMember).toList();
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
