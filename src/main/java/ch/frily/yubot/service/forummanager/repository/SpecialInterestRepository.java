package ch.frily.yubot.service.forummanager.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.EntityMapper;
import ch.frily.yubot.database.IRepository;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * "Special Interest"-Repository
 * <p>
 *     Takes care of the database connections
 * </p>
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
@Slf4j
public class SpecialInterestRepository implements IRepository {


    @Override
    public Table getTable() {
        return Table.SPECIAL_INTEREST;
    }

    public static @Nullable SpecialInterest getInterestById(int id) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.SPECIAL_INTEREST);
        query.select();
        query.where(Table.SpecialInterestColumn.ID, DatabaseQuery.Operator.EQUALS, id);

        ResultSet rs = query.executeDataQuery();

        List<SpecialInterest> result = EntityMapper.build(rs, SpecialInterest.class);
        if (result.isEmpty()) {
            return null;
        } else {
            return result.getFirst();
        }
    }

    public static List<SpecialInterest> getInterests() throws SQLException {
        return getInterests(null);
    }

    /**
     * Request all interests with a given {@link SpecialInterest.RequestStatus RequestStatus}.
     * @param requestStatus the status you want the interests to have. If null, it returns any status
     * @return
     * @throws SQLException
     */
    public static List<SpecialInterest> getInterests(@Nullable SpecialInterest.RequestStatus requestStatus) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.SPECIAL_INTEREST);
        query.select();
        if (Objects.nonNull(requestStatus)) {
            query.where(Table.SpecialInterestColumn.REQUEST_STATUS, DatabaseQuery.Operator.EQUALS, requestStatus.name());
        }

        query.orderBy(Table.SpecialInterestColumn.ID, DatabaseQuery.OrderBy.ASCENDED);

        ResultSet rs = query.executeDataQuery();

        return EntityMapper.build(rs, SpecialInterest.class);
    }


    /**
     * Get all special interets from a given user
     * @param user
     * @return
     * @throws SQLException
     */
    public static List<SpecialInterest> getInterestsByUser(User user) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.SPECIAL_INTEREST);
        query.select();
        query.where(Table.SpecialInterestColumn.ORIGINAL_POSTER_ID, DatabaseQuery.Operator.EQUALS, user.getId());
        query.orderBy(Table.SpecialInterestColumn.ID, DatabaseQuery.OrderBy.ASCENDED);

        ResultSet rs = query.executeDataQuery();

        return EntityMapper.build(rs, SpecialInterest.class);
    }

    public static int createInterest(SpecialInterest interest) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.SPECIAL_INTEREST);
        query.insert(Table.SpecialInterestColumn.NAME, interest.getName());
        query.insert(Table.SpecialInterestColumn.DESCRIPTION, interest.getDescription());
        query.insert(Table.SpecialInterestColumn.REQUEST_STATUS, SpecialInterest.RequestStatus.PENDING.name());
        query.insert(Table.SpecialInterestColumn.ORIGINAL_POSTER_ID, interest.getOriginalPoster().getId());
        query.insert(Table.SpecialInterestColumn.NOTIFY_OP, interest.isNotifyOP());
        query.setReturning(Table.SpecialInterestColumn.ID);

        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            return rs.getInt(Table.SpecialInterestColumn.ID.getColumn());
        } else {
            throw new InvalidStateException("Interest failed to insert properly!");
        }


    }

    public static void updateInterest(SpecialInterest interest) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.SPECIAL_INTEREST);
        query.update(Table.SpecialInterestColumn.NAME, interest.getName());
        query.update(Table.SpecialInterestColumn.DESCRIPTION, interest.getDescription());
        query.update(Table.SpecialInterestColumn.REQUEST_STATUS, interest.getRequestStatus().name());
        query.update(Table.SpecialInterestColumn.NOTIFY_OP, interest.isNotifyOP());

        if (interest.getPost() != null) {
            query.update(Table.SpecialInterestColumn.POST_ID, interest.getPost().getId());
        }
        if (interest.getModerator() != null) {
            query.update(Table.SpecialInterestColumn.MODERATOR_ID, interest.getModerator().getId());
        }
        if (interest.getDenyReason() != null) {
            query.update(Table.SpecialInterestColumn.DENY_REASON, interest.getDenyReason());
        }

        query.where(Table.SpecialInterestColumn.ID, DatabaseQuery.Operator.EQUALS, interest.getId());

        query.executeQuery();
    }
}
