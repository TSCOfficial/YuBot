package ch.frily.yubot.feature;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class ProfileRepository {

    public static Profile getProfile(Member member) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, member.getId());
        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            boolean activeModSendInDm = rs.getBoolean(Table.ProfileColumn.ACTIVEMOD_SEND_IN_DM.getColumn());
            return new Profile(member, activeModSendInDm);
        }
        return null;
    }

    public static Profile getProfileOrThrow(Member member) throws InvalidStateException, SQLException, ClassNotFoundException {
        Profile profile = getProfile(member);
        if (profile == null) {
            throw new InvalidStateException("No profile found");
        }
        return profile;
    }

    public static void upsertSetting(Member member, Setting setting, Object value) throws SQLException, ClassNotFoundException {
        createProfileIfMissing(member);
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, member.getId());
        query.update(setting.getColumn, value);
        query.executeQuery();
    }

    public static void createProfile(Member member) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.insert(Table.ProfileColumn.MEMBER_ID, member.getId());
        query.executeQuery();
    }

    private static void createProfileIfMissing(Member member) throws SQLException, ClassNotFoundException {
        if (getProfile(member) == null) {
            createProfile(member);
        }
    }
}
