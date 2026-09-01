package ch.frily.yubot.feature.ticket;

import lombok.Getter;

public enum TicketStatus {
    NEW("👋", true, true),
    CLAIMED("🎫", true, false),
    CLOSED("🔻", false, false);

    // Ticket TextChannel name icon
    @Getter
    private final String icon;

    // Allow the ticket to be closes / a close request to be executed
    @Getter
    private final boolean allowClose;

    // Allow the ticket to be claimed
    @Getter
    private final boolean allowClaim;

    TicketStatus(String icon, boolean allowClose, boolean allowClaim){
        this.icon = icon;
        this.allowClose = allowClose;
        this.allowClaim = allowClaim;
    }
}
