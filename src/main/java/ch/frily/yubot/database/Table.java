package ch.frily.yubot.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Table {
    TICKET("ticket", TicketColumn.class),
    TICKET_TYPE_CONTROL("ticket_type_control", TicketTypeControlColumn.class),
    ACTIVE_MOD("active_mod", ActiveModColumn.class),
    ACTIVE_MOD_TRACKING("active_mod_tracking", ActiveModTrackingColumn.class),
    DYNAMIC_MESSAGE("dynamic_message", DynamicMessageColumn.class),
    ABSENCE("absence", AbsenceColumn.class),
    SETTING("setting", SettingColumn.class),
    PROFILE("profile", ProfileColumn.class),
    EVENT_REMINDER("event_reminder", EventReminderColumn.class);

    private final String table;
    private final Class<? extends Column> columnClass;

    /**
     * Common interface
     */
    public interface Column { // todo check if replaceable by @Getter?
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
        UPDATED_AT("updated_at"),
        IS_REMINDER_SENT("is_reminder_sent");
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

    // ACTIVE MOD
    @Getter
    @RequiredArgsConstructor
    public enum ActiveModColumn implements Column {
        MODERATOR_ID("moderator_id"),
        LAST_ACTIVITY_AT("last_activity_at"),
        ACTIVITY_REQUESTED_AT("activity_requested_at"),
        ACTIVITY_REQUEST_MESSAGE_ID("activity_request_message_id"),
        REQUESTED_ATTENTION_MESSAGE_ID("requested_attention_message_id");
        private final String column;
    }

    // ACTIVE MOD TRACKING
    @Getter
    @RequiredArgsConstructor
    public enum ActiveModTrackingColumn implements Column {
        MODERATOR_ID("moderator_id"),
        ACTIVE_TIME("active_time"),
        LAST_TIME_ACTIVE("last_time_active"),
        MONTH("month"),
        MISSED_ACTIVITY_REQUEST_COUNT("missed_activity_request_count"),
        TOTAL_ACTIVITY_REQUEST_COUNT("total_activity_request_count");
        private final String column;
    }

    // ABSCENCE
    @Getter
    @RequiredArgsConstructor
    public enum AbsenceColumn implements Column {
        ID("id"),
        MEMBER_ID("member_id"),
        START_DATETIME("start_datetime"),
        END_DATETIME("end_datetime"),
        TYPE("type"),
        REASON("reason"),
        SEND_NOTICE("send_notice"),
        CREATED_AT("created_at"),
        UPDATED_AT("updated_at");
        private final String column;
    }

    // USER-SETTING
    @Getter
    @RequiredArgsConstructor
    public enum SettingColumn implements Column {
        MEMBER_ID("member_id"),
        ACTIVEMOD_SEND_IN_DM("activemod_send_in_dm"),
        ABSENCE_NOTICE("absence_notice");
        private final String column;
    }

    @Getter
    @RequiredArgsConstructor
    public enum ProfileColumn implements Column {
        PROFILE_ID("profile_id"),
        ACCOUNT_ID("account_id"),
        NAME("name"),
        IS_CURRENTLY_USED("is_currently_used");
        private final String column;
    }

    // EVENT REMINDER
    @Getter
    @RequiredArgsConstructor
    public enum EventReminderColumn implements Column {
        ID("id"),
        IS_REMINDER_SENT("is_reminder_sent");
        private final String column;
    }
}