package ch.frily.yubot.feature;

import ch.frily.yubot.Client;
import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AbsenceRepository {

    public static List<Absence> getAbsences() throws SQLException, ClassNotFoundException {
        List<Absence> absences = new ArrayList<>();

        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.select();
        ResultSet rs = query.executeDataQuery();
        while (rs.next()) {
            int id = rs.getInt(Table.AbsenceColumn.ID.getColumn());
            String memberId = rs.getString(Table.AbsenceColumn.MEMBER_ID.getColumn());
            LocalDateTime startDateTime = rs.getTimestamp(Table.AbsenceColumn.START_DATETIME.getColumn()).toLocalDateTime();
            LocalDateTime endDateTime = rs.getTimestamp(Table.AbsenceColumn.END_DATETIME.getColumn()).toLocalDateTime();
            String reason = rs.getString(Table.AbsenceColumn.REASON.getColumn());
            boolean sendNotice = rs.getBoolean(Table.AbsenceColumn.SEND_NOTICE.getColumn());

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            Member member = guild.getMemberById(memberId);
            Absence absence = new Absence(id, member, startDateTime, endDateTime, reason, sendNotice);
            absences.add(absence);
        }
        return absences;
    }

    public static Absence getAbsenceById(int id) throws SQLException, ClassNotFoundException, NoSuchMethodException {

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

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            Member member = guild.getMemberById(memberId);
            Absence absence = new Absence(id, member, startDateTime, endDateTime, reason, sendNotice);
            return absence;
        }
        throw new NoSuchMethodException("Abwesenheit nicht gefunden");
    }

    private static void createAbsence(Absence absence) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.insert(Table.AbsenceColumn.MEMBER_ID, absence.member().getId());
        query.insert(Table.AbsenceColumn.START_DATETIME, absence.fromDateTime());
        query.insert(Table.AbsenceColumn.END_DATETIME, absence.toDateTime());
        query.insert(Table.AbsenceColumn.REASON, absence.reason());
        query.insert(Table.AbsenceColumn.SEND_NOTICE, absence.absenceMessage());

        query.executeQuery();
    }

    private static void updateAbsence(Absence absence) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ABSENCE);
        query.update(Table.AbsenceColumn.START_DATETIME, absence.fromDateTime());
        query.update(Table.AbsenceColumn.END_DATETIME, absence.toDateTime());
        query.update(Table.AbsenceColumn.REASON, absence.reason());
        query.update(Table.AbsenceColumn.SEND_NOTICE, absence.absenceMessage());
        query.where(Table.AbsenceColumn.ID, DatabaseQuery.Operator.EQUALS, absence.id());

        query.executeQuery();
    }

    public static void upsertAbsence(Absence absence) throws SQLException, ClassNotFoundException {
        if (absence.id() == null) {
            log.info("Creating absence for member {}", absence.member().getId());
            createAbsence(absence);
        } else {
            log.info("Updating absence for member {}", absence.member().getId());
            updateAbsence(absence);
        }
    }
}
