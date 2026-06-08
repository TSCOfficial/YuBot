package ch.frily.yubot.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Table {
    TICKET("ticket", TicketColumn.class);

    private final String table;
    private final Class<? extends Column> columnClass;

    /**
     * Common interface
     */
    public interface Column {
        String getColumn();
    }

    // TICKET
    @Getter
    @RequiredArgsConstructor
    public enum TicketColumn implements Column {
        ID("id"),
        OWNER_ID("owner_id"),
        ASSIGNEE_ID("assignee_id"),
        CHANNEL_ID("channel_id"),
        TYPE("type"),
        LAST_ACTIVITY_AT("last_activity_at"),
        WELCOME_MESSAGE_ID("welcome_message_id"),
        IS_REQUEST_PENDING("is_request_pending"),
        CLOSE_REQUEST_COUNT("close_request_count"),
        STATUS("status");
        private final String column;
    }
}