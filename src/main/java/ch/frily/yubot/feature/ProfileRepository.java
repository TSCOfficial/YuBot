package ch.frily.yubot.feature;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static void setActiveModSendInDm(Member member, boolean activeModSendInDm) throws SQLException, ClassNotFoundException {
        createProfileIfMissing(member);
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, member.getId());
        query.update(Table.ProfileColumn.ACTIVEMOD_SEND_IN_DM, activeModSendInDm);
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
