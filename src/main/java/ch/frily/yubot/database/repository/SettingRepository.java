package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.feature.setting.Settings;
import ch.frily.yubot.feature.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Slf4j
public class SettingRepository {

    /**
     * Get the settings of a given member
     * @param member The member to get the settings from
     * @return a {@link Settings}-record with the member's settings, or <code>null</code> if no setting-record found
     * @throws SQLException Database failure
     */
    @Nullable
    public static Settings getSettings(@Nullable Member member) throws SQLException {
        Objects.requireNonNull(member);

        DatabaseQuery query = new DatabaseQuery(Table.SETTING);
        query.where(Table.SettingColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, member.getId());
        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            Boolean activeModSendInDm = rs.getBoolean(Table.SettingColumn.ACTIVEMOD_SEND_IN_DM.getColumn());
            String absenceNotice = rs.getString(Table.SettingColumn.ABSENCE_NOTICE.getColumn());
            return new Settings(member, activeModSendInDm, absenceNotice);
        }
        return null;
    }

    /**
     * Get the settings of a given member
     * @param member The member to get the settings from
     * @return a {@link Settings}-record with the member's settings
     * @throws SQLException Database failure
     * @throws InvalidStateException When the member doesn't have a {@link Settings}-record in the database
     */
    @NotNull
    public static Settings getSettingsOrThrow(Member member) throws InvalidStateException, SQLException {
        Settings settings = getSettings(member);
        if (settings == null) {
            throw new InvalidStateException("No settings found");
        }
        return settings;
    }

    /**
     * Get the value of a given setting from a user
     * @param member the member to get the setting from
     * @param setting the setting to get from the member
     * @return the string value or null if the setting is not found
     * @throws SQLException Database failure
     */
    public static <T> T getSetting(Member member, Setting setting, Class<T> dataType) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.SETTING);
        query.select(setting.getDbColumn());
        query.where(Table.SettingColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, member.getId());
        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            return rs.getObject(setting.getDbColumn().getColumn(), dataType);
        }
        return null;
    }

    public static void upsertSetting(Member member, Setting setting, Object value) throws SQLException {
        createProfileIfMissing(member);
        DatabaseQuery query = new DatabaseQuery(Table.SETTING);
        query.where(Table.SettingColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, member.getId());
        query.update(setting.getDbColumn(), value);
        query.executeQuery();
    }

    public static void createProfile(Member member) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.SETTING);
        query.insert(Table.SettingColumn.MEMBER_ID, member.getId());
        query.executeQuery();
    }

    private static void createProfileIfMissing(Member member) throws SQLException {
        if (getSettings(member) == null) {
            createProfile(member);
        }
    }
}
