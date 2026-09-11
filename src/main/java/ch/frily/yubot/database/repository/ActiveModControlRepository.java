package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ActiveModControlRepository {
    public static void updateControl(Boolean state) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_CONTROL);
        query.update(Table.ActiveModControlColumn.ALLOW_OPTIN, state);
        query.executeQuery();
    }

    /**
     * Whether the activemod control is enabled or disabled
     * @return True if mods can opt-in, false if not
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static boolean isOptInAllowed() throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.ACTIVE_MOD_CONTROL);
        query.select(Table.ActiveModControlColumn.ALLOW_OPTIN);
        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            return rs.getBoolean(Table.ActiveModControlColumn.ALLOW_OPTIN.getColumn());
        }
        return false;
    }
}
