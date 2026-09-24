package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.feature.profile.ProfileHistory;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * The profile history saves the changes made to the names of any profiles
 * <p>
 *     This is primarily used to lookup profile-owners even when they changed the name of their profile
 * </p>
 */
public class ProfileHistoryRepository {

    public static void create(Profile oldProfile, Profile newProfile) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE_HISTORY);
        query.insert(Table.ProfileHistoryColumn.PROFILE_ID, oldProfile.profileId());
        query.insert(Table.ProfileHistoryColumn.PREVIOUS_NAME, oldProfile.name());
        query.insert(Table.ProfileHistoryColumn.NEW_NAME, newProfile.name());
        query.insert(Table.ProfileHistoryColumn.PARENT_ID, oldProfile.parentAccount().getId());
        query.insert(Table.ProfileHistoryColumn.CREATED_AT, LocalDateTime.now());

        query.executeQuery();
    }

    public static ProfileHistory getByName(String name) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE_HISTORY);
        query.where(Table.ProfileHistoryColumn.PREVIOUS_NAME, DatabaseQuery.Operator.EQUALS, name);

        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            int id = rs.getInt(Table.ProfileHistoryColumn.ID.getColumn());
            String previousName = rs.getString(Table.ProfileHistoryColumn.PREVIOUS_NAME.getColumn());
            String newName = rs.getString(Table.ProfileHistoryColumn.NEW_NAME.getColumn());
            String parentId = rs.getString(Table.ProfileHistoryColumn.PARENT_ID.getColumn());
            String profileId = rs.getString(Table.ProfileHistoryColumn.PROFILE_ID.getColumn());
            LocalDateTime createdAt =  rs.getTimestamp(Table.ProfileHistoryColumn.CREATED_AT.getColumn()).toLocalDateTime();

            Member parentAccount = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(parentId);
            Profile profileRef = ProfileRepository.getProfileById(profileId);

            return new ProfileHistory(id, previousName, newName, profileRef, parentAccount, createdAt);
        }
        throw new NotFoundException(String.format("Profilhistorie für Profil '%s' nicht gefunden.", name));
    }
}
