package ch.frily.yubot.feature;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClosureRepository {

    public static List<ActiveMod> getModerators() throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.CLOSURE);

        ResultSet resultSet = query.select().executeDataQuery();

        List<ActiveMod> activeMods = new ArrayList<>();
        while (resultSet.next()) {
            long modId = resultSet.getLong(Table.ClosureColumn.MODERATOR_ID.getColumn());
            LocalDateTime lastActivityAt = resultSet.getTimestamp(Table.ClosureColumn.LAST_ACTIVITY_AT.getColumn()).toLocalDateTime();
            Timestamp activityRequestedAtTimestamp = resultSet.getTimestamp(Table.ClosureColumn.ACTIVITY_REQUESTED_AT.getColumn());
            long activityRequestMessageId = resultSet.getLong(Table.ClosureColumn.ACTIVITY_REQUEST_MESSAGE_ID.getColumn());

            LocalDateTime activityRequestedAt = activityRequestedAtTimestamp == null ? null : activityRequestedAtTimestamp.toLocalDateTime();

            Member moderator = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(modId);
            ActiveMod activeMod = new ActiveMod(moderator, lastActivityAt, activityRequestedAt, activityRequestMessageId);
            activeMods.add(activeMod);
        }
        return activeMods;
    }

    public static ActiveMod getModerator(Member member) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.CLOSURE);
        query.select().where(Table.ClosureColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, member.getIdLong());

        ResultSet resultSet = query.executeDataQuery();

        if (!resultSet.next()) {
            return null;
        }
        LocalDateTime lastActivityAt = resultSet.getTimestamp(Table.ClosureColumn.LAST_ACTIVITY_AT.getColumn()).toLocalDateTime();
        Timestamp activityRequestedAtTimestamp = resultSet.getTimestamp(Table.ClosureColumn.ACTIVITY_REQUESTED_AT.getColumn());
        long activityRequestMessageId = resultSet.getLong(Table.ClosureColumn.ACTIVITY_REQUEST_MESSAGE_ID.getColumn());

        return new ActiveMod(member, lastActivityAt, activityRequestedAtTimestamp == null ? null : activityRequestedAtTimestamp.toLocalDateTime(), activityRequestMessageId);
    }

    /**
     * Get the {@link ActiveMod} by the activity-request message id
     * <p></p>
     * Useful when you need to get the moderator by a button action on a message
     * @param messageId
     * @return
     * @throws SQLException
     */
    public static ActiveMod getModeratorByRequestMessageId(long messageId) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.CLOSURE);
        query.select().where(Table.ClosureColumn.ACTIVITY_REQUEST_MESSAGE_ID, DatabaseQuery.Operator.EQUALS, messageId);

        ResultSet resultSet = query.executeDataQuery();

        if (!resultSet.next()) {
            return null;
        }
        Member member = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(resultSet.getLong(Table.ClosureColumn.MODERATOR_ID.getColumn()));
        LocalDateTime lastActivityAt = resultSet.getTimestamp(Table.ClosureColumn.LAST_ACTIVITY_AT.getColumn()).toLocalDateTime();
        Timestamp activityRequestedAtTimestamp = resultSet.getTimestamp(Table.ClosureColumn.ACTIVITY_REQUESTED_AT.getColumn());
        long activityRequestMessageId = resultSet.getLong(Table.ClosureColumn.ACTIVITY_REQUEST_MESSAGE_ID.getColumn());

        return new ActiveMod(member, lastActivityAt, activityRequestedAtTimestamp == null ? null : activityRequestedAtTimestamp.toLocalDateTime(), activityRequestMessageId);
    }

    public static void deleteModerator(Member member) {
        DatabaseQuery query = new DatabaseQuery(Table.CLOSURE);
        query.where(Table.ClosureColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, member.getIdLong()).delete();
        query.executeQuery();
    }

    public static void createModerator(Member member) {
        DatabaseQuery query = new DatabaseQuery(Table.CLOSURE);
        query.insert(Table.ClosureColumn.MODERATOR_ID, member.getIdLong());
        query.insert(Table.ClosureColumn.LAST_ACTIVITY_AT, LocalDateTime.now());
        query.executeQuery();
    }

    public static void updateModerator(ActiveMod activeMod) {
        DatabaseQuery query = new DatabaseQuery(Table.CLOSURE);
        query.update(Table.ClosureColumn.LAST_ACTIVITY_AT, activeMod.lastActivityAt());
        query.update(Table.ClosureColumn.ACTIVITY_REQUESTED_AT, activeMod.activityRequestedAt());
        query.update(Table.ClosureColumn.ACTIVITY_REQUEST_MESSAGE_ID, activeMod.activityRequestMessageId());
        query.where(Table.ClosureColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, activeMod.member().getIdLong());

        query.executeQuery();
    }

    public static void updateModeratorActivity(Member member) {
        ActiveMod activeMod = new ActiveMod(member, LocalDateTime.now(), null, null);
        updateModerator(activeMod);
    }
}
