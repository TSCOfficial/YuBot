package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.feature.profile.ProfileHistory;
import ch.frily.yubot.feature.profile.ProfileMessage;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * The profile history saves the changes made to the names of any profiles
 * <p>
 *     This is primarily used to lookup profile-owners even when they changed the name of their profile
 * </p>
 */
public class ProfileMessageRepository {

    public static void create(ProfileMessage profileMessage) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE_MESSAGE);
        query.insert(Table.ProfileMessageColumn.PROFILE_ID, profileMessage.profile().profileId());
        query.insert(Table.ProfileMessageColumn.MESSAGE_ID, profileMessage.message().getId());
        query.insert(Table.ProfileMessageColumn.CHANNEL_ID, profileMessage.channel().getId());

        query.executeQuery();
    }

    /**
     * Gets the {@link ProfileMessage} using the channel & message ID
     * @param message
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static ProfileMessage get(Message message) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.PROFILE_MESSAGE);
        query.where(Table.ProfileMessageColumn.CHANNEL_ID, DatabaseQuery.Operator.EQUALS, message.getChannel().getId());
        query.where(Table.ProfileMessageColumn.MESSAGE_ID, DatabaseQuery.Operator.EQUALS, message.getId());

        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            String profileId = rs.getString(Table.ProfileMessageColumn.PROFILE_ID.getColumn());

            Profile profileRef = ProfileRepository.getProfileById(profileId);

            return new ProfileMessage(message, profileRef, message.getChannel());
        }
        throw new NotFoundException(String.format("Webhook-Nachricht mit ID '%s/%s' nicht gefunden.", message.getChannel().getId(),  message.getId()));
    }

    /**
     * Checks whether the member is the owner of a given message
     * @return
     */
    public static boolean isOwner(Member member, Message message) throws SQLException, ClassNotFoundException {
        Profile profile = get(message).profile();

        if (member.getId().equals(profile.parentAccount().getId())) {
            return true;
        } else {
            return false;
        }
    }
}
