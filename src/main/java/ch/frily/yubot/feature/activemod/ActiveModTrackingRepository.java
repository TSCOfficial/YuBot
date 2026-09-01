package ch.frily.yubot.feature.activemod;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class ActiveModTrackingRepository {

    public static Map<Member, List<ActiveModTracking>> completeWithMissingModerators(Map<Member, List<ActiveModTracking>> groupedActiveMods) {
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        List<Member> allModerators = guild.getMembersWithRoles(EnvResolver.getRoleById(EnvKey.ROLE_MODERATOR));
        Map<Member, List<ActiveModTracking>> completeWithMissingModerators = new LinkedHashMap<>();

        allModerators.forEach(moderator -> {
            List<ActiveModTracking> activeModTrackings = groupedActiveMods.get(moderator);
            if (activeModTrackings == null) {
                activeModTrackings = new ArrayList<>();
            }
            completeWithMissingModerators.put(moderator, activeModTrackings);
        });

        return completeWithMissingModerators.entrySet().stream()
                .sorted(Comparator.comparingInt(
                        (Map.Entry<Member, List<ActiveModTracking>> entry) ->
                                entry.getValue().stream().mapToInt(ActiveModTracking::activeTime).sum()
                ).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

    }

    public static Map<Member, List<ActiveModTracking>> getActiveModTrackingsAsMap() throws SQLException, ClassNotFoundException {
        List <ActiveModTracking> activeModTrackings = getActiveModTrackings();
        Map<Member, List<ActiveModTracking>> groupedActiveMods = new LinkedHashMap<>();

        activeModTrackings.forEach(activeModTracking -> {
            List<ActiveModTracking> groupedTrackings = groupedActiveMods.get(activeModTracking.moderator());
            if (groupedTrackings == null) {
                groupedTrackings = new ArrayList<>();
                groupedActiveMods.put(activeModTracking.moderator(), groupedTrackings);
            }
            groupedTrackings.add(activeModTracking);
            groupedActiveMods.replace(activeModTracking.moderator(), groupedTrackings);
        });

        // sort by total time
        return groupedActiveMods.entrySet().stream()
                .sorted(Comparator.comparingInt(
                        (Map.Entry<Member, List<ActiveModTracking>> entry) ->
                                entry.getValue().stream().mapToInt(ActiveModTracking::activeTime).sum()
                ).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    };

    /**
     * Get all active moderators from the database.
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static List<ActiveModTracking> getActiveModTrackings() throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_TRACKING);
        ResultSet resultSet = query.select().executeDataQuery();

        List<ActiveModTracking> activeModTrackings = new java.util.ArrayList<>();
        while (resultSet.next()) {
            int activeTime = resultSet.getInt(Table.ActiveModTrackingColumn.ACTIVE_TIME.getColumn());
            long moderatorId = resultSet.getLong(Table.ActiveModTrackingColumn.MODERATOR_ID.getColumn());
            LocalDateTime lastTimeActive = resultSet.getTimestamp(Table.ActiveModTrackingColumn.LAST_TIME_ACTIVE.getColumn()).toLocalDateTime();
            LocalDate localDateMonth = resultSet.getDate(Table.ActiveModTrackingColumn.MONTH.getColumn()).toLocalDate();
            YearMonth month = YearMonth.from(localDateMonth); // convert from LocalDate to YearMonth
            int missedActivityRequestCount = resultSet.getInt(Table.ActiveModTrackingColumn.MISSED_ACTIVITY_REQUEST_COUNT.getColumn());
            int totalActivityRequestCount = resultSet.getInt(Table.ActiveModTrackingColumn.TOTAL_ACTIVITY_REQUEST_COUNT.getColumn());

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            Member member = guild.getMemberById(moderatorId);
            activeModTrackings.add(new ActiveModTracking(member, activeTime, lastTimeActive, month, missedActivityRequestCount, totalActivityRequestCount));
        }
        return activeModTrackings;
    }

    /**
     * Get an active moderator's tracking data for a given month.
     * @param member
     * @param atMonth
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static ActiveModTracking getActiveModTracking(Member member, YearMonth atMonth) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_TRACKING);
        query.select();
        query.where(Table.ActiveModTrackingColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, member.getIdLong());
        query.where(Table.ActiveModTrackingColumn.MONTH, DatabaseQuery.Operator.EQUALS, atMonth.atDay(1));
        ResultSet resultSet = query.executeDataQuery();

        if (resultSet.next()) {
            int activeTime = resultSet.getInt(Table.ActiveModTrackingColumn.ACTIVE_TIME.getColumn());
            LocalDateTime lastTimeActive = resultSet.getTimestamp(Table.ActiveModTrackingColumn.LAST_TIME_ACTIVE.getColumn()).toLocalDateTime();
            LocalDate localDateMonth = resultSet.getDate(Table.ActiveModTrackingColumn.MONTH.getColumn()).toLocalDate();
            YearMonth month = YearMonth.from(localDateMonth); // convert from LocalDate to YearMonth
            int missedActivityRequestCount = resultSet.getInt(Table.ActiveModTrackingColumn.MISSED_ACTIVITY_REQUEST_COUNT.getColumn());
            int totalActivityRequestCount = resultSet.getInt(Table.ActiveModTrackingColumn.TOTAL_ACTIVITY_REQUEST_COUNT.getColumn());
            return new ActiveModTracking(member, activeTime, lastTimeActive, month, missedActivityRequestCount, totalActivityRequestCount);
        }

        throw new NullPointerException("ActiveModTracking for " + member.getIdLong() + " not found.");
    }

    /**
     * Create or update an existing ActiveModTracking entry for a given moderator.
     * <p>
     *     Using the {@link YearMonth}, the system checks for an existing activemod-tracking entry of given month (at day 1).
     *     If there is no entry, a new entry is created for the current month.
     * </p>
     * @param member
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void upsertActiveMod(Member member) throws SQLException, ClassNotFoundException {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        try {
            ActiveModTracking tracking = getActiveModTracking(member, YearMonth.now());
            if (!tracking.lastTimeActive().isBefore(now)) {
                return;
            }
            ActiveModTracking updated = new ActiveModTracking(
                    member, tracking.activeTime() + 1, now, tracking.month(), tracking.missedActivityRequestCount(), tracking.totalActivityRequestCount()
            );
            updateActiveModTracking(updated);
        } catch (NullPointerException e) {
            createActiveModTracking(member);
        }
    }

    public static void incrementMissedActivityRequestCount(Member member) throws SQLException, ClassNotFoundException {

        ActiveModTracking tracking = getActiveModTracking(member, YearMonth.now());
        ActiveModTracking updated = new ActiveModTracking(
                member, tracking.activeTime(), tracking.lastTimeActive(), tracking.month(), tracking.missedActivityRequestCount() + 1, tracking.totalActivityRequestCount()
        );
        updateActiveModTracking(updated);
    }

    public static void incrementTotalActivityRequestCount(Member member) throws SQLException, ClassNotFoundException {

        ActiveModTracking tracking = getActiveModTracking(member, YearMonth.now());
        ActiveModTracking updated = new ActiveModTracking(
                member, tracking.activeTime(), tracking.lastTimeActive(), tracking.month(), tracking.missedActivityRequestCount(), tracking.totalActivityRequestCount() + 1
        );
        updateActiveModTracking(updated);
    }

    /**
     * Create a new ActiveModTracking entry
     * <p>
     *     Month is set to the first day of the current month to be compatible with the DB's date type.
     * </p>
     * @param member The activemod to create a tracking entry for
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void createActiveModTracking(Member member) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_TRACKING);
        query.insert(Table.ActiveModTrackingColumn.MODERATOR_ID, member.getIdLong());
        query.insert(Table.ActiveModTrackingColumn.MONTH, YearMonth.now().atDay(1));
        query.insert(Table.ActiveModTrackingColumn.LAST_TIME_ACTIVE, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        query.executeQuery();
    }

    public static void updateActiveModTracking(ActiveModTracking activeModTracking) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_TRACKING);
        query.update(Table.ActiveModTrackingColumn.ACTIVE_TIME, activeModTracking.activeTime());
        query.update(Table.ActiveModTrackingColumn.LAST_TIME_ACTIVE, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        query.where(Table.ActiveModTrackingColumn.MODERATOR_ID, DatabaseQuery.Operator.EQUALS, activeModTracking.moderator().getIdLong());
        query.where(Table.ActiveModTrackingColumn.MONTH, DatabaseQuery.Operator.EQUALS, activeModTracking.month().atDay(1));
        query.executeQuery();
    }
}
