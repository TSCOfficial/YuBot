package ch.frily.yubot.feature;

import ch.frily.yubot.Client;
import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
public class AbsenceRepository {

    /**
     * Get all absences grouped together for each day
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Map<LocalDate, List<Absence>> getAbsencesPerDay() throws SQLException, ClassNotFoundException {
        return groupByDay(getAbsences());
    }

    /**
     * Get all absences grouped together for each day for a specific member
     * @param forMember
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Map<LocalDate, List<Absence>> getAbsencesPerDay(@Nullable Member forMember) throws SQLException, ClassNotFoundException {
        return groupByDay(getAbsences(forMember));
    }

    /**
     * Get all absences
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static List<Absence> getAbsences() throws SQLException, ClassNotFoundException {
        return getAbsences(null);
    }

    /**
     * Get all absences for a specific member
     * @param forMember
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static List<Absence> getAbsences(@Nullable Member forMember) throws SQLException, ClassNotFoundException {
        List<Absence> absences = new ArrayList<>();

        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.select();
        if (forMember != null) {
            query.where(Table.AbsenceColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, forMember.getId());
        }
        query.where(Table.AbsenceColumn.END_DATETIME, DatabaseQuery.Operator.GREATER_OR_EQUAL, LocalDateTime.now());

        ResultSet rs = query.executeDataQuery();
        while (rs.next()) {
            int id = rs.getInt(Table.AbsenceColumn.ID.getColumn());
            String memberId = rs.getString(Table.AbsenceColumn.MEMBER_ID.getColumn());
            LocalDateTime startDateTime = rs.getTimestamp(Table.AbsenceColumn.START_DATETIME.getColumn()).toLocalDateTime();
            LocalDateTime endDateTime = rs.getTimestamp(Table.AbsenceColumn.END_DATETIME.getColumn()).toLocalDateTime();
            String reason = rs.getString(Table.AbsenceColumn.REASON.getColumn());
            boolean sendNotice = rs.getBoolean(Table.AbsenceColumn.SEND_NOTICE.getColumn());
            LocalDateTime createdAt = rs.getTimestamp(Table.AbsenceColumn.CREATED_AT.getColumn()).toLocalDateTime();
            LocalDateTime updatedAt = rs.getTimestamp(Table.AbsenceColumn.UPDATED_AT.getColumn()).toLocalDateTime();

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            Member member = guild.getMemberById(memberId);
            Absence absence = new Absence(id, member, startDateTime, endDateTime, reason, sendNotice, createdAt, updatedAt);
            absences.add(absence);
        }
        return absences;
    }

    /**
     * Get an absence by its id
     * @param id
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public static Absence getAbsenceById(int id) throws SQLException, ClassNotFoundException, NullPointerException {

        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.select();
        query.where(Table.AbsenceColumn.ID, DatabaseQuery.Operator.EQUALS, id);
        ResultSet rs = query.executeDataQuery();
        if (rs.next()) {
            String memberId = rs.getString(Table.AbsenceColumn.MEMBER_ID.getColumn());
            LocalDateTime startDateTime = rs.getTimestamp(Table.AbsenceColumn.START_DATETIME.getColumn()).toLocalDateTime();
            LocalDateTime endDateTime = rs.getTimestamp(Table.AbsenceColumn.END_DATETIME.getColumn()).toLocalDateTime();
            String reason = rs.getString(Table.AbsenceColumn.REASON.getColumn());
            boolean sendNotice = rs.getBoolean(Table.AbsenceColumn.SEND_NOTICE.getColumn());
            LocalDateTime createdAt = rs.getTimestamp(Table.AbsenceColumn.CREATED_AT.getColumn()).toLocalDateTime();
            LocalDateTime updatedAt = rs.getTimestamp(Table.AbsenceColumn.UPDATED_AT.getColumn()).toLocalDateTime();

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            Member member = guild.getMemberById(memberId);
            Absence absence = new Absence(id, member, startDateTime, endDateTime, reason, sendNotice, createdAt, updatedAt);
            return absence;
        }
        throw new NullPointerException("Abwesenheit nicht gefunden");
    }

    /**
     * Create an absence
     * @param absence
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private static void createAbsence(Absence absence) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.insert(Table.AbsenceColumn.MEMBER_ID, absence.member().getId());
        query.insert(Table.AbsenceColumn.START_DATETIME, absence.fromDateTime());
        query.insert(Table.AbsenceColumn.END_DATETIME, absence.toDateTime());
        query.insert(Table.AbsenceColumn.REASON, absence.reason());
        query.insert(Table.AbsenceColumn.SEND_NOTICE, absence.absenceMessage());

        query.executeQuery();
    }

    /**
     * Update an absence
     * @param absence
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private static void updateAbsence(Absence absence) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.update(Table.AbsenceColumn.START_DATETIME, absence.fromDateTime());
        query.update(Table.AbsenceColumn.END_DATETIME, absence.toDateTime());
        query.update(Table.AbsenceColumn.REASON, absence.reason());
        query.update(Table.AbsenceColumn.SEND_NOTICE, absence.absenceMessage());
        query.update(Table.AbsenceColumn.UPDATED_AT, LocalDateTime.now());
        query.where(Table.AbsenceColumn.ID, DatabaseQuery.Operator.EQUALS, absence.id());

        query.executeQuery();
    }

    /**
     * Create or update an absence
     * <p>
     *     Whether the given absence should be created or overwrite an existing one depends if the absence has an id
     * </p>
     * @param absence
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void upsertAbsence(Absence absence) throws SQLException, ClassNotFoundException {
        if (absence.id() == null) {
            createAbsence(absence);
        } else {
            updateAbsence(absence);
        }
    }

    /**
     * Split every absence into day-absences and group them by their day
     * @param absences The absences to convert - may contain absences spanning multiple days
     * @return The day-absences of each day, sorted by day and by start time within a day
     */
    private static Map<LocalDate, List<Absence>> groupByDay(List<Absence> absences) {
        Map<LocalDate, List<Absence>> groupedAbsences = new TreeMap<>();

        absences.stream()
                .flatMap(absence -> splitIntoDayAbsences(absence).stream())
                .sorted(Comparator.comparing(Absence::fromDateTime))
                .forEach(dayAbsence -> groupedAbsences
                        .computeIfAbsent(dayAbsence.fromDateTime().toLocalDate(), day -> new ArrayList<>())
                        .add(dayAbsence));
        return groupedAbsences;
    }

    /**
     * Convert an absence into one absence per day for multi-day absences
     * <p></p>
     * The original start- and end-time are kept on the first and the last day, every day in between
     * covers the whole day. A single-day absence is returned unchanged
     * @param absence The absence to split
     * @return One day-absence per covered day, empty if the absence ends before it starts
     */
    private static List<Absence> splitIntoDayAbsences(Absence absence) {
        List<Absence> splitAbsences = new ArrayList<>();

        LocalDate firstDay = absence.fromDateTime().toLocalDate();
        LocalDate lastDay = absence.toDateTime().toLocalDate();

        for (LocalDate day = firstDay; !day.isAfter(lastDay); day = day.plusDays(1)) {
            LocalDateTime fromDateTime = day.equals(firstDay) ? absence.fromDateTime() : day.atStartOfDay();
            LocalDateTime toDateTime = day.equals(lastDay) ? absence.toDateTime() : day.atTime(LocalTime.MAX);

            splitAbsences.add(new Absence(
                    absence.id(),
                    absence.member(),
                    fromDateTime,
                    toDateTime,
                    absence.reason(),
                    absence.absenceMessage(),
                    absence.createdAt(),
                    absence.updatedAt()
            ));
        }

        return splitAbsences;
    }
}
