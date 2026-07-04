package ch.frily.yubot.feature;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ActiveModTrackingRepository {

    public static List<ActiveModTracking> getActiveModTrackings() throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_TRACKING);
        ResultSet resultSet = query.select().executeDataQuery();

        List<ActiveModTracking> activeModTrackings = new java.util.ArrayList<>();
        while (resultSet.next()) {
            int activeTime = resultSet.getInt(Table.ActiveModTrackingColumn.ACTIVE_TIME.getColumn());
            long moderatorId = resultSet.getLong(Table.ActiveModTrackingColumn.MODERATOR_ID.getColumn());

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            Member member = guild.getMemberById(moderatorId);
            activeModTrackings.add(new ActiveModTracking(member, activeTime));
        }
        return activeModTrackings;
    }

    public static ActiveModTracking getActiveModTracking(Member member) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_TRACKING);
        query.select().where(Table.ActiveModTrackingColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, member.getIdLong());
        ResultSet resultSet = query.executeDataQuery();

        if (resultSet.next()) {
            int activeTime = resultSet.getInt(Table.ActiveModTrackingColumn.ACTIVE_TIME.getColumn());
            return new ActiveModTracking(member, activeTime);
        }

        throw new NullPointerException("ActiveModTracking for " + member.getIdLong() + " not found.");
    }

    public static void upsertActiveMod(Member member) throws SQLException, ClassNotFoundException {
        try {
            ActiveModTracking activeModTracking = getActiveModTracking(member);
            ActiveModTracking updatedTracking = new ActiveModTracking(member, activeModTracking.activeTime() + 1);
            updateActiveModTracking(updatedTracking);
        } catch (NullPointerException e) {
            createActiveModTracking(member);
        }
    }

    public static void createActiveModTracking(Member member) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_TRACKING);
        query.insert(Table.ActiveModTrackingColumn.MODERATOR_ID, member.getIdLong());
        query.executeQuery();
    }

    public static void updateActiveModTracking(ActiveModTracking activeModTracking) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_TRACKING);
        query.update(Table.ActiveModTrackingColumn.ACTIVE_TIME, activeModTracking.activeTime());
        query.where(Table.ActiveModTrackingColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, activeModTracking.moderator().getIdLong());
        query.executeQuery();
    }
}
