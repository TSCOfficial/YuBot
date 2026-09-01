package ch.frily.yubot.feature.activemod;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActiveModRepository {

    public static List<ActiveMod> getModerators() throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD);

        ResultSet resultSet = query.select().executeDataQuery();

        List<ActiveMod> activeMods = new ArrayList<>();
        while (resultSet.next()) {
            long modId = resultSet.getLong(Table.ActiveModColumn.MODERATOR_ID.getColumn());
            LocalDateTime lastActivityAt = resultSet.getTimestamp(Table.ActiveModColumn.LAST_ACTIVITY_AT.getColumn()).toLocalDateTime();
            Timestamp activityRequestedAtTimestamp = resultSet.getTimestamp(Table.ActiveModColumn.ACTIVITY_REQUESTED_AT.getColumn());
            long activityRequestMessageId = resultSet.getLong(Table.ActiveModColumn.ACTIVITY_REQUEST_MESSAGE_ID.getColumn());
            long requestedAttentionMessageId = resultSet.getLong(Table.ActiveModColumn.REQUESTED_ATTENTION_MESSAGE_ID.getColumn());

            LocalDateTime activityRequestedAt = activityRequestedAtTimestamp == null ? null : activityRequestedAtTimestamp.toLocalDateTime();

            Member moderator = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(modId);
            ActiveMod activeMod = new ActiveMod(moderator, lastActivityAt, activityRequestedAt, activityRequestMessageId, requestedAttentionMessageId);
            activeMods.add(activeMod);
        }
        return activeMods;
    }

    /**
     * Check if there are any current active moderators
     * <br>
     * Useful if you want to check if ther server is open or closed
     * @return True if there are active moderators (server opened), false if not (server closed)
     */
    public static boolean hasActiveModerators(){
        try {
            return !getModerators().isEmpty();
        } catch (Exception exception) {
            return ExceptionHandler.fail(exception);
        }

    }

    public static ActiveMod getModerator(Member member) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD);
        query.select().where(Table.ActiveModColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, member.getIdLong());

        ResultSet resultSet = query.executeDataQuery();

        if (!resultSet.next()) {
            return null;
        }
        LocalDateTime lastActivityAt = resultSet.getTimestamp(Table.ActiveModColumn.LAST_ACTIVITY_AT.getColumn()).toLocalDateTime();
        Timestamp activityRequestedAtTimestamp = resultSet.getTimestamp(Table.ActiveModColumn.ACTIVITY_REQUESTED_AT.getColumn());
        long activityRequestMessageId = resultSet.getLong(Table.ActiveModColumn.ACTIVITY_REQUEST_MESSAGE_ID.getColumn());
        long requestedAttentionMessageId = resultSet.getLong(Table.ActiveModColumn.REQUESTED_ATTENTION_MESSAGE_ID.getColumn());

        return new ActiveMod(member, lastActivityAt, activityRequestedAtTimestamp == null ? null : activityRequestedAtTimestamp.toLocalDateTime(), activityRequestMessageId, requestedAttentionMessageId);
    }

    /**
     * Get the {@link ActiveMod} by the activity-request message id
     * <p></p>
     * Useful when you need to get the moderator by a button action on a message
     * @param messageId
     * @return
     * @throws SQLException
     */
    public static ActiveMod getModeratorByActivityRequestMessageId(long messageId) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD);
        query.select().where(Table.ActiveModColumn.ACTIVITY_REQUEST_MESSAGE_ID, DatabaseQuery.Operator.EQUALS, messageId);

        ResultSet resultSet = query.executeDataQuery();

        if (!resultSet.next()) {
            return null;
        }
        Member member = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(resultSet.getLong(Table.ActiveModColumn.MODERATOR_ID.getColumn()));
        LocalDateTime lastActivityAt = resultSet.getTimestamp(Table.ActiveModColumn.LAST_ACTIVITY_AT.getColumn()).toLocalDateTime();
        Timestamp activityRequestedAtTimestamp = resultSet.getTimestamp(Table.ActiveModColumn.ACTIVITY_REQUESTED_AT.getColumn());
        long activityRequestMessageId = resultSet.getLong(Table.ActiveModColumn.ACTIVITY_REQUEST_MESSAGE_ID.getColumn());
        long requestedAttentionMessageId = resultSet.getLong(Table.ActiveModColumn.REQUESTED_ATTENTION_MESSAGE_ID.getColumn());

        return new ActiveMod(member, lastActivityAt, activityRequestedAtTimestamp == null ? null : activityRequestedAtTimestamp.toLocalDateTime(), activityRequestMessageId, requestedAttentionMessageId);
    }

    public static List<ActiveMod> getModeratorsWithRequestedAttentionMessageId() throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD);
        query.where(Table.ActiveModColumn.REQUESTED_ATTENTION_MESSAGE_ID, DatabaseQuery.Operator.NOT_EQUALS, 0L);
        query.select();
        ResultSet resultSet = query.executeDataQuery();

        List<ActiveMod> activeMods = new ArrayList<>();
        while (resultSet.next()) {
            Member member = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(resultSet.getLong(Table.ActiveModColumn.MODERATOR_ID.getColumn()));
            LocalDateTime lastActivityAt = resultSet.getTimestamp(Table.ActiveModColumn.LAST_ACTIVITY_AT.getColumn()).toLocalDateTime();
            Timestamp activityRequestedAtTimestamp = resultSet.getTimestamp(Table.ActiveModColumn.ACTIVITY_REQUESTED_AT.getColumn());
            long activityRequestMessageId = resultSet.getLong(Table.ActiveModColumn.ACTIVITY_REQUEST_MESSAGE_ID.getColumn());
            long requestedAttentionMessageId = resultSet.getLong(Table.ActiveModColumn.REQUESTED_ATTENTION_MESSAGE_ID.getColumn());
            ActiveMod activeMod = new ActiveMod(member, lastActivityAt, activityRequestedAtTimestamp == null ? null : activityRequestedAtTimestamp.toLocalDateTime(), activityRequestMessageId, requestedAttentionMessageId);
            activeMods.add(activeMod);
        }

        return activeMods;
    }

    public static void deleteModerator(Member member) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD);
        query.where(Table.ActiveModColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, member.getIdLong()).delete();
        query.executeQuery();
    }

    public static void createModerator(Member member) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD);
        query.insert(Table.ActiveModColumn.MODERATOR_ID, member.getIdLong());
        query.insert(Table.ActiveModColumn.LAST_ACTIVITY_AT, LocalDateTime.now());
        query.executeQuery();
    }

    public static void updateModerator(ActiveMod activeMod) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD);
        query.update(Table.ActiveModColumn.LAST_ACTIVITY_AT, activeMod.lastActivityAt());
        query.update(Table.ActiveModColumn.ACTIVITY_REQUESTED_AT, activeMod.activityRequestedAt());
        query.update(Table.ActiveModColumn.ACTIVITY_REQUEST_MESSAGE_ID, activeMod.activityRequestMessageId());
        query.update(Table.ActiveModColumn.REQUESTED_ATTENTION_MESSAGE_ID, activeMod.requestedAttentionMessageId());
        query.where(Table.ActiveModColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, activeMod.member().getIdLong());

        query.executeQuery();
    }

    public static void updateModeratorActivity(Member member) throws SQLException, ClassNotFoundException {
        ActiveMod activeMod = new ActiveMod(member, LocalDateTime.now(), null, null, null);
        updateModerator(activeMod);
    }
}
