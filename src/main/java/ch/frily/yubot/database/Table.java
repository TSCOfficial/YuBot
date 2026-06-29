package ch.frily.yubot.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Table {
    TICKET("ticket", TicketColumn.class),
    TICKET_TYPE_CONTROL("ticket_type_control", TicketTypeControlColumn.class),
    CLOSURE("closure", ClosureColumn.class),
    DYNAMIC_MESSAGE("dynamic_message", DynamicMessageColumn.class);

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
        OWNER_ID("owner_id"),
        ASSIGNEE_ID("assignee_id"),
        CHANNEL_ID("channel_id"),
        TYPE("type"),
        LAST_ACTIVITY_AT("last_activity_at"),
        WELCOME_MESSAGE_ID("welcome_message_id"),
        IS_REQUEST_PENDING("is_request_pending"),
        CLOSE_REQUEST_COUNT("close_request_count"),
        STATUS("status"),
        UPDATED_AT("updated_at");
        private final String column; // todo define second field: datatype, so that building an object can be automatized over any table
    }

    // TICKET CONTROL
    @Getter
    @RequiredArgsConstructor
    public enum TicketTypeControlColumn implements Column {
        TYPE("type"),
        IS_LOCKED("is_locked");
        private final String column;
    }

    // DYNAMIC MESSAGES
    @Getter
    @RequiredArgsConstructor
    public enum DynamicMessageColumn implements Column {
        NAME("name"),
        CHANNEL_ID("channel_id"),
        MESSAGE_ID("message_id");
        private final String column;
    }

    // CLOSURE
    @Getter
    @RequiredArgsConstructor
    public enum ClosureColumn implements Column {
        MODERATOR_ID("moderator_id"),
        LAST_ACTIVITY_AT("last_activity_at"),
        ACTIVITY_REQUESTED_AT("activity_requested_at"),
        ACTIVITY_REQUEST_MESSAGE_ID("activity_request_message_id"),;
        private final String column;
    }
}