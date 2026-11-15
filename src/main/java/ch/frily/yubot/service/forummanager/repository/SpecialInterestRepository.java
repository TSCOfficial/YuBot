package ch.frily.yubot.service.forummanager.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.IRepository;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.service.forummanager.entity.SpecialInterestEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * "Special Interest"-Repository
 * <p>
 *     Takes care of the database connections
 * </p>
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class SpecialInterestRepository implements IRepository {


    @Override
    public Table getTable() {
        return Table.SPECIAL_INTEREST;
    }

    public List<SpecialInterestEntity> getSpecialInterests() throws SQLException {
        DatabaseQuery query = new DatabaseQuery(getTable());
        query.select();

        ResultSet rs = query.executeDataQuery();

        List<SpecialInterestEntity> specialInterests = new ArrayList<>();
        while (rs.next()) {
            String name = rs.getString("name");
            String description = rs.getString("description");
            String statusKey = rs.getString("status");

            Objects.requireNonNull(name);
            Objects.requireNonNull(description);
            Objects.requireNonNull(statusKey);

            SpecialInterestEntity.RequestStatus requestStatus = SpecialInterestEntity.RequestStatus.valueOf(statusKey);

            SpecialInterestEntity interest = new SpecialInterestEntity();
            interest.setName(name);
            interest.setDescription(description);
            interest.setRequestStatus(requestStatus);

            specialInterests.add(interest);
        }
        return specialInterests;
    }
}
