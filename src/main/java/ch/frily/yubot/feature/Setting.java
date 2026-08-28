package ch.frily.yubot.feature;

import ch.frily.yubot.database.Table;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;
import java.util.List;

/**
 * Maps the settings to all required generic utilities
 */
public enum Setting {
    ACTIVEMOD_SEND_IN_DM(
            "aktivitätsbestätigungsanfrage",
            "Entscheide wo deine ActiveMod-Nachrichten gesendet werden.",
            Table.ProfileColumn.ACTIVEMOD_SEND_IN_DM,
            Boolean.class,
            List.of(EnvKey.ROLE_MODERATOR)
    ),
    TESTTTT(
            "test123",
            "nur ein test",
            Table.AbsenceColumn.REASON,
            String.class,
            List.of(EnvKey.ROLE_MODERATOR),
            List.of("Hiiii", "hellloooo")
    )
    ;

    @Getter
    String label;
    @Getter
    String description;
    @Getter
    Table.Column getColumn;
    @Getter
    Class<?> dataType;
    @Getter
    List<Role> allowedRoles;
    @Getter
    List<String> autocompleteOptions;

    /**
     * Define a Setting without autocomplete options
     * <p>
     *     This is primarly used for boolean-settings, because boolean inputs automaticly show true/false.
     * </p>
     * @param label
     * @param description
     * @param dbColumn database column to be able to resolve for generic actions
     * @param dataType The type of the setting
     * @param <T>
     */
    <T> Setting(String label, String description, Table.Column dbColumn, Class<T> dataType, List<EnvKey> allowedRoles){
        this.label = label;
        this.description = description;
        this.getColumn = dbColumn;
        this.dataType = dataType;
        this.allowedRoles = allowedRoles.stream().map(EnvResolver::getRoleById).toList();
    }

    /**
     * Define a Setting with autocomplete options
     * @param label
     * @param description
     * @param dbColumn database column to be able to resolve for generic actions
     * @param dataType The type of the setting
     * @param options Selectable types for that setting in the given dataType
     * @param <T>
     */
    <T> Setting(String label, String description, Table.Column dbColumn, Class<T> dataType, List<EnvKey> allowedRoles, List<T> options){
        this.label = label;
        this.description = description;
        this.getColumn = dbColumn;
        this.dataType = dataType;
        this.allowedRoles = allowedRoles.stream().map(EnvResolver::getRoleById).toList();
        this.autocompleteOptions = options.stream().map(Object::toString).toList();
    }

    public static Setting getSettingByLabel(String label){
        return Arrays.stream(Setting.values()).filter(setting -> setting.getLabel().equals(label)).findFirst().orElse(null);
    }

//    private void validateEnumEntries() {
//        Arrays.stream(Setting.values()).forEach(setting -> {
//            if(setting.autocompleteOptions != null && setting.dataType != String.class){
//                throw new IllegalArgumentException("Autocomplete options are only allowed for String settings");
//            }
//            if (setting.autocompleteOptions == null && setting.dataType == String.class){
//                throw new IllegalArgumentException("Autocomplete options are required for String settings");
//            }
//        });
//    }
}


