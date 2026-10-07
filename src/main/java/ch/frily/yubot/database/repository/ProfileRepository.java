package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.service.profile.Profile;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class ProfileRepository {

    public static Profile getProfileById(String id) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.ID, DatabaseQuery.Operator.EQUALS, id);
        query.orderBy(Table.ProfileColumn.NAME, DatabaseQuery.OrderBy.ASCENDED);
        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            String profileId = rs.getString(Table.ProfileColumn.ID.getColumn());
            String memberId = rs.getString(Table.ProfileColumn.PARENT_ID.getColumn());
            String name = rs.getString(Table.ProfileColumn.NAME.getColumn());
            boolean isCurrentlyUsed = rs.getBoolean(Table.ProfileColumn.IS_CURRENTLY_USED.getColumn());
            String profilePicture = rs.getString(Table.ProfileColumn.PROFILEPICTURE.getColumn());
            String proxy = rs.getString(Table.ProfileColumn.PROXY.getColumn());
            int useCount = rs.getInt(Table.ProfileColumn.USE_COUNT.getColumn());

            Member member = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(memberId);

            return new Profile(profileId, member, name, isCurrentlyUsed, profilePicture, proxy, useCount);
        }
        throw new NotFoundException(String.format("Profil mit ID '%s' nicht gefunden.", id));
    }

    public static List<Profile> getProfilesFromAccount(Member member) throws SQLException {
        Objects.requireNonNull(member);
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.PARENT_ID, DatabaseQuery.Operator.EQUALS, member.getId());
        ResultSet rs = query.executeDataQuery();

        List<Profile> profiles = new ArrayList<>();
        while (rs.next()) {
            String profileId = rs.getString(Table.ProfileColumn.ID.getColumn());
            String name = rs.getString(Table.ProfileColumn.NAME.getColumn());
            boolean isCurrentlyUsed = rs.getBoolean(Table.ProfileColumn.IS_CURRENTLY_USED.getColumn());
            String profilePicture = rs.getString(Table.ProfileColumn.PROFILEPICTURE.getColumn());
            String proxy = rs.getString(Table.ProfileColumn.PROXY.getColumn());
            int useCount = rs.getInt(Table.ProfileColumn.USE_COUNT.getColumn());

            profiles.add(new Profile(profileId, member, name, isCurrentlyUsed, profilePicture, proxy, useCount));
        }

        return profiles;
    }

    public static Optional<Profile> getCurrentUserProfile(Member member) throws SQLException {
        List<Profile> profiles = getProfilesFromAccount(member);

        if  (profiles.isEmpty()) {
            return Optional.empty();
        } else {
            return profiles.stream().filter(Profile::isCurrentlyUsed).findFirst();
        }
    }

    public static void createProfile(Profile profile) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.insert(Table.ProfileColumn.ID, profile.profileId());
        query.insert(Table.ProfileColumn.PARENT_ID, profile.parentAccount().getId());
        query.insert(Table.ProfileColumn.NAME, profile.name());
        query.insert(Table.ProfileColumn.IS_CURRENTLY_USED, profile.isCurrentlyUsed());
        query.insert(Table.ProfileColumn.PROFILEPICTURE, profile.profilePicture());
        query.insert(Table.ProfileColumn.PROXY, profile.proxy());
        query.insert(Table.ProfileColumn.USE_COUNT, profile.useCount());
        query.executeQuery();
    }

    /**
     * Select a profile and automaticly deselect any other profile
     * @param selectedProfile the profile to select
     * @throws SQLException Database failure
     */
    public static void selectProfile(Profile selectedProfile) throws SQLException {

        // clear profile usage from all member's profile
        Member member = selectedProfile.parentAccount();
        unselectProfiles(member);

        // mark selected profile as in use
        updateProfileUsage(selectedProfile, true);
    }

    /**
     * Unselect all profiles of a member
     * <p>
     *     This sets the "is in use" flag to false for every profile linked to the member's account
     * </p>
     * @param member the member to unselect the profiles from
     * @throws SQLException Database failure
     */
    public static void unselectProfiles(Member member) throws SQLException {
        getProfilesFromAccount(member).forEach(profile -> {
            try {
                updateProfileUsage(profile, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void updateProfile(Profile profile)  throws SQLException {
        recordToHistory(profile);

        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.ID, DatabaseQuery.Operator.EQUALS, profile.profileId());
        query.update(Table.ProfileColumn.NAME, profile.name());
        query.update(Table.ProfileColumn.IS_CURRENTLY_USED, profile.isCurrentlyUsed());
        query.update(Table.ProfileColumn.PROFILEPICTURE, profile.profilePicture());
        query.update(Table.ProfileColumn.PROXY, profile.proxy());
        query.update(Table.ProfileColumn.USE_COUNT, profile.useCount());
        query.executeQuery();
    }

    /**
     * Markes a profile as in use or not in use
     * <p>
     *     If the profile is set to use, the use count of given profile is incremented
     * </p>
     * @param profile the profile to update
     * @param isInUse whether the profile should be set to "in use" (true) or "not in use" (false)
     * @throws SQLException Database failure
     */
    private static void updateProfileUsage(Profile profile, boolean isInUse)  throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE);
        query.where(Table.ProfileColumn.ID, DatabaseQuery.Operator.EQUALS, profile.profileId());
        query.update(Table.ProfileColumn.IS_CURRENTLY_USED, isInUse);

        if (isInUse) {
            query.update(Table.ProfileColumn.USE_COUNT, profile.useCount() + 1);
        }
        query.executeQuery();
    }

    public static List<Profile> orderByUsage(List<Profile> profiles) {
        return profiles.stream().sorted(Comparator.comparingInt(Profile::useCount)).toList().reversed();
    }

    private static void recordToHistory(Profile newProfile) throws SQLException {
        Profile oldProfile = getProfileById(newProfile.profileId());

        if (!oldProfile.name().equals(newProfile.name())) {
            ProfileHistoryRepository.create(oldProfile, newProfile);
        }
    }
}
