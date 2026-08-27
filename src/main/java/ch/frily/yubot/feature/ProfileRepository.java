package ch.frily.yubot.feature;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ProfileRepository {

    /**
     * Maps the settings to all required generic utilities
     */
    public enum Setting {
        ACTIVEMOD_SEND_IN_DM("aktivitätsbestätigungsanfrage", Table.ProfileColumn.ACTIVEMOD_SEND_IN_DM, Boolean.class),
        TESTTTTT("test_1_2", Table.ProfileColumn.ACTIVEMOD_SEND_IN_DM, String.class, List.of("test", "test2"));

        @Getter
        String label;
        @Getter
        Table.Column getColumn;
        @Getter
        Class<?> dataType;
        @Getter
        List<String> autocompleteOptions;

        <T> Setting(String label, Table.Column dbColumn, Class<T> dataType){
            this.label = label;
            this.getColumn = dbColumn;
            this.dataType = dataType;
        }
        <T> Setting(String label, Table.Column dbColumn, Class<T> dataType, List<T> options){
            this.label = label;
            this.getColumn = dbColumn;
            this.dataType = dataType;
            this.autocompleteOptions = options.stream().map(Object::toString).toList();
        }
    }

    public static Setting getSettingByLabel(String label){
        return Arrays.stream(Setting.values()).filter(setting -> setting.getLabel().equals(label)).findFirst().orElse(null);
    }



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

    public static void setSetting(Member member, Setting setting, Object value) throws SQLException, ClassNotFoundException {
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
