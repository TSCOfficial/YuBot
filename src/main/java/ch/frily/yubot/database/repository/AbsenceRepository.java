package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.service.absence.Absence;
import ch.frily.yubot.service.absence.AbsenceType;
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
     * @return a map of all absences grouped by day
     * @throws SQLException Database failure
     */
    public static Map<LocalDate, List<Absence>> getAbsencesPerDay() throws SQLException {
        return groupByDay(getAbsences());
    }

    /**
     * Get all absences grouped together for each day for a specific member
     * @param forMember the member to get the absences from
     * @return a map of the absences of a member, grouped by day
     * @throws SQLException Database failure
     */
    public static Map<LocalDate, List<Absence>> getAbsencesPerDay(@Nullable Member forMember) throws SQLException {
        return groupByDay(getAbsences(forMember));
    }

    /**
     * Get all absences
     * @return all raw, existing absences
     * @throws SQLException Database failure
     */
    public static List<Absence> getAbsences() throws SQLException {
        return getAbsences(null);
    }

    /**
     * Get all absences for a specific member
     * @param forMember the member to get the absences from
     * @return raw absence list of a member
     * @throws SQLException Database failure
     */
    public static List<Absence> getAbsences(@Nullable Member forMember) throws SQLException {
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
            String typeString = rs.getString(Table.AbsenceColumn.TYPE.getColumn());
            String reason = rs.getString(Table.AbsenceColumn.REASON.getColumn());
            boolean sendNotice = rs.getBoolean(Table.AbsenceColumn.SEND_NOTICE.getColumn());
            LocalDateTime createdAt = rs.getTimestamp(Table.AbsenceColumn.CREATED_AT.getColumn()).toLocalDateTime();
            LocalDateTime updatedAt = rs.getTimestamp(Table.AbsenceColumn.UPDATED_AT.getColumn()).toLocalDateTime();
            AbsenceType absenceType = AbsenceType.valueOf(typeString);

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            Member member = guild.getMemberById(memberId);
            Absence absence = new Absence(id, member, startDateTime, endDateTime, absenceType, reason, sendNotice, createdAt, updatedAt);
            absences.add(absence);
        }
        return absences;
    }

    /**
     * Get an absence by its id
     * @param id the id of the absence to get
     * @return the absence matching the given id
     * @throws SQLException Database failure
     * @throws ch.frily.yubot.exception.NotFoundException Absence not found
     */
    public static Absence getAbsenceById(int id) throws SQLException, NotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.select();
        query.where(Table.AbsenceColumn.ID, DatabaseQuery.Operator.EQUALS, id);
        ResultSet rs = query.executeDataQuery();
        if (rs.next()) {
            String memberId = rs.getString(Table.AbsenceColumn.MEMBER_ID.getColumn());
            LocalDateTime startDateTime = rs.getTimestamp(Table.AbsenceColumn.START_DATETIME.getColumn()).toLocalDateTime();
            LocalDateTime endDateTime = rs.getTimestamp(Table.AbsenceColumn.END_DATETIME.getColumn()).toLocalDateTime();
            String typeString = rs.getString(Table.AbsenceColumn.TYPE.getColumn());
            String reason = rs.getString(Table.AbsenceColumn.REASON.getColumn());
            boolean sendNotice = rs.getBoolean(Table.AbsenceColumn.SEND_NOTICE.getColumn());
            LocalDateTime createdAt = rs.getTimestamp(Table.AbsenceColumn.CREATED_AT.getColumn()).toLocalDateTime();
            LocalDateTime updatedAt = rs.getTimestamp(Table.AbsenceColumn.UPDATED_AT.getColumn()).toLocalDateTime();
            AbsenceType absenceType = AbsenceType.valueOf(typeString);

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            Member member = guild.getMemberById(memberId);
            return new Absence(id, member, startDateTime, endDateTime, absenceType, reason, sendNotice, createdAt, updatedAt);
        }
        throw new NotFoundException("Abwesenheit nicht gefunden");
    }

    /**
     * Get the absences for a given date span
     * @param startDateTimeSearch when to start search span
     * @param endDateTimeSearch when to end the search span
     * @return a list of absences that start or end during the given search span
     * @throws SQLException database failure
     */
    public static List<Absence> getAbsencesByDateSpan(LocalDateTime startDateTimeSearch, LocalDateTime endDateTimeSearch) throws SQLException {
        List<Absence> absences = new ArrayList<>();

        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.select();
        query.where(Table.AbsenceColumn.START_DATETIME, DatabaseQuery.Operator.LESS_OR_EQUAL, endDateTimeSearch);
        query.where(Table.AbsenceColumn.END_DATETIME, DatabaseQuery.Operator.GREATER_OR_EQUAL, startDateTimeSearch);

        ResultSet rs = query.executeDataQuery();
        while (rs.next()) {
            int id = rs.getInt(Table.AbsenceColumn.ID.getColumn());
            String memberId = rs.getString(Table.AbsenceColumn.MEMBER_ID.getColumn());
            LocalDateTime startDateTime = rs.getTimestamp(Table.AbsenceColumn.START_DATETIME.getColumn()).toLocalDateTime();
            LocalDateTime endDateTime = rs.getTimestamp(Table.AbsenceColumn.END_DATETIME.getColumn()).toLocalDateTime();
            String typeString = rs.getString(Table.AbsenceColumn.TYPE.getColumn());
            String reason = rs.getString(Table.AbsenceColumn.REASON.getColumn());
            boolean sendNotice = rs.getBoolean(Table.AbsenceColumn.SEND_NOTICE.getColumn());
            LocalDateTime createdAt = rs.getTimestamp(Table.AbsenceColumn.CREATED_AT.getColumn()).toLocalDateTime();
            LocalDateTime updatedAt = rs.getTimestamp(Table.AbsenceColumn.UPDATED_AT.getColumn()).toLocalDateTime();
            AbsenceType absenceType = AbsenceType.valueOf(typeString);

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            Member member = guild.getMemberById(memberId);
            Absence absence = new Absence(id, member, startDateTime, endDateTime, absenceType, reason, sendNotice, createdAt, updatedAt);

            if (member == null) {
                // delete absence record when the user has left the server
                deleteAbsenceById(id);
                continue;
            }
            absences.add(absence);
        }
        return absences;
    }

    public static List<Absence> getAbsencesByMemberAndDateSpan(Member member, LocalDateTime startDateTimeSearch, LocalDateTime endDateTimeSearch) throws SQLException {
        return getAbsencesByDateSpan(startDateTimeSearch, endDateTimeSearch).stream().filter(absence -> absence.member().getId().equals(member.getId())).toList();
    }

    /**
     * Create an absence
     * @param absence the absence to create
     * @throws SQLException Database failure
     */
    private static void createAbsence(Absence absence) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.insert(Table.AbsenceColumn.MEMBER_ID, absence.member().getId());
        query.insert(Table.AbsenceColumn.START_DATETIME, absence.fromDateTime());
        query.insert(Table.AbsenceColumn.END_DATETIME, absence.toDateTime());
        query.insert(Table.AbsenceColumn.TYPE, absence.type().name());
        query.insert(Table.AbsenceColumn.REASON, absence.reason());
        query.insert(Table.AbsenceColumn.SEND_NOTICE, absence.absenceMessage());

        query.executeQuery();
    }

    /**
     * Update an absence
     * @param absence the absence to update
     * @throws SQLException Database failure
     */
    private static void updateAbsence(Absence absence) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.update(Table.AbsenceColumn.START_DATETIME, absence.fromDateTime());
        query.update(Table.AbsenceColumn.END_DATETIME, absence.toDateTime());
        query.update(Table.AbsenceColumn.TYPE, absence.type().name());
        query.update(Table.AbsenceColumn.REASON, absence.reason());
        query.update(Table.AbsenceColumn.SEND_NOTICE, absence.absenceMessage());
        query.update(Table.AbsenceColumn.UPDATED_AT, LocalDateTime.now());
        query.where(Table.AbsenceColumn.ID, DatabaseQuery.Operator.EQUALS, absence.id());

        query.executeQuery();
    }

    /**
     * Delete an absence
     * @param absenceId the id of the absence to delete
     * @throws SQLException Database failure
     */
    public static void deleteAbsenceById(int absenceId) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.delete();
        query.where(Table.AbsenceColumn.ID, DatabaseQuery.Operator.EQUALS, absenceId);
        query.executeQuery();
    }

    /**
     * Create or update an absence
     * <p>
     *     Whether the given absence should be created or overwrite an existing one depends if the absence has an id
     * </p>
     * @param absence the absence to upsert
     * @throws SQLException Database failure
     */
    public static void upsertAbsence(Absence absence) throws SQLException {
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
    public static Map<LocalDate, List<Absence>> groupByDay(List<Absence> absences) {
        Map<LocalDate, List<Absence>> groupedAbsences = new TreeMap<>();

        absences.stream()
                .flatMap(absence -> splitIntoDayAbsences(absence).stream())
                .sorted(Comparator.comparing(Absence::fromDateTime))
                .forEach(dayAbsence -> {
                    if (!dayAbsence.fromDateTime().toLocalDate().isBefore(LocalDate.now())) { // only add the ones that are today or in the future (-> not before today)
                        groupedAbsences
                                .computeIfAbsent(dayAbsence.fromDateTime().toLocalDate(), day -> new ArrayList<>())
                                .add(dayAbsence);
                    }
                });
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
                    absence.type(),
                    absence.reason(),
                    absence.absenceMessage(),
                    absence.createdAt(),
                    absence.updatedAt()
            ));
        }

        return splitAbsences;
    }
}
