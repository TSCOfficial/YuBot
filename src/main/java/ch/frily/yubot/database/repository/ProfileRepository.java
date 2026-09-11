package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileRepository {

    public static Profile getProfileById(String id) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.PROFILE_ID, DatabaseQuery.Operator.EQUALS, id);
        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            String profileId = rs.getString(Table.ProfileColumn.PROFILE_ID.getColumn());
            String memberId = rs.getString(Table.ProfileColumn.ACCOUNT_ID.getColumn());
            String name = rs.getString(Table.ProfileColumn.NAME.getColumn());
            boolean isCurrentlyUsed = rs.getBoolean(Table.ProfileColumn.IS_CURRENTLY_USED.getColumn());

            Member member = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(memberId);

            return new Profile(profileId, member, name, isCurrentlyUsed);
        }
        throw new InvalidStateException("Profile not found");
    }
    public static List<Profile> getProfilesFromAccount(Member member) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.ACCOUNT_ID, DatabaseQuery.Operator.EQUALS, member.getId());
        ResultSet rs = query.executeDataQuery();

        List<Profile> profiles = new ArrayList<>();
        while (rs.next()) {
            String profileId = rs.getString(Table.ProfileColumn.PROFILE_ID.getColumn());
            String name = rs.getString(Table.ProfileColumn.NAME.getColumn());
            boolean isCurrentlyUsed = rs.getBoolean(Table.ProfileColumn.IS_CURRENTLY_USED.getColumn());

            profiles.add(new Profile(profileId, member, name, isCurrentlyUsed));
        }

        return profiles;
    }

    public static void createProfile(Profile profile) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.insert(Table.ProfileColumn.PROFILE_ID, profile.profileId());
        query.insert(Table.ProfileColumn.ACCOUNT_ID, profile.parentAccount().getId());
        query.insert(Table.ProfileColumn.NAME, profile.name());
        query.insert(Table.ProfileColumn.IS_CURRENTLY_USED, profile.isCurrentlyUsed());
        query.executeQuery();
    }

    public static void updateProfile(Profile profile)  throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.PROFILE_ID, DatabaseQuery.Operator.EQUALS, profile.profileId());
        query.update(Table.ProfileColumn.NAME, profile.name());
        query.update(Table.ProfileColumn.IS_CURRENTLY_USED, profile.isCurrentlyUsed());
        query.executeQuery();
    }
}
