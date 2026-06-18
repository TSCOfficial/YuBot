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

        query.executeQuery();
    }
}
