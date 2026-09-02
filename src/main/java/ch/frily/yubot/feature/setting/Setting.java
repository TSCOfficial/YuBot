package ch.frily.yubot.feature.setting;

import ch.frily.yubot.database.Table;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

;

public enum Setting {
    ACTIVEMOD_SEND_IN_DM(
            "aktivitätsbestätigungsanfrage",
            "Entscheide wo deine ActiveMod-Nachrichten gesendet werden.",
            Table.SettingColumn.ACTIVEMOD_SEND_IN_DM,
            List.of(EnvKey.ROLE_MODERATOR),
            Boolean.class,
            List.of(
                    new SettingOption<>("Via Server", false, "Sendet dir Nachricht in #active-moderation (standard)"),
                    new SettingOption<>("Via DM", true, "Sendet die Nachrichten per DM")
            )
    ),
    ABSENCE_NOTICE(
            "abwesenheitsmeldung",
            "Nachricht welche bei @Erwähnungen während deiner Absenz gesendet wird.",
            Table.SettingColumn.ABSENCE_NOTICE,
            List.of(EnvKey.ROLE_YUTEAM, EnvKey.ROLE_TWITCHMOD),
            String.class,
            10,
            100
    );

    @Getter
    String label;
    @Getter
    String description;
    @Getter
    Table.Column dbColumn;
    @Getter
    Class<?> dataType;
    @Getter
    List<Role> allowedRoles;
    @Getter
    List<SettingOption<?>> autocompleteOptions;
    /** Minimal allowed characters for a string*/
    @Getter
    int min;
    /** Maximal allowed characters for a string*/
    @Getter
    int max;

    /**
     * Define a Setting without autocomplete options
     * <p>
     *     This is primarily used for boolean-settings, because boolean inputs automatically show true/false.
     * </p>
     * @param label
     * @param description
     * @param dbColumn database column to be able to resolve for generic actions
     * @param dataType The type of the setting
     * @param <T>
     */
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, Class<T> dataType, int min, int max){
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.dataType = dataType;
        this.allowedRoles = allowedRoles.stream().map(EnvResolver::getRoleById).toList();
        this.min = min;
        this.max = max;
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
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, Class<T> dataType, List<SettingOption<?>> options){
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.dataType = dataType;
        this.allowedRoles = allowedRoles.stream().map(EnvResolver::getRoleById).toList();
        this.autocompleteOptions = options;
    }

    public static Setting getSettingByLabel(String label){
        return Arrays.stream(Setting.values()).filter(setting -> setting.getLabel().equals(label)).findFirst().orElse(null);
    }

    /**
     * Get the option by it's option-label
     * <p>
     *     This use primarily used for the discord autocomplete feature.
     *     The autocomplete shows the label and needs to be converted to its corresponding value for the database.
     * </p>
     * @param label
     * @param dataType
     * @return
     * @param <T>
     */
    public <T> SettingOption<T> getOptionByLabel(String label, Class<T> dataType) {
        Optional<SettingOption<?>> option = this.autocompleteOptions.stream()
                .filter(o -> o.label().equals(label))
                .findFirst();

        if (option.isEmpty()) {
            throw new IllegalArgumentException("No option found for label: " + label);
        }

        T value = dataType.cast(option.get().value());
        return new SettingOption<>(option.get().label(), value, option.get().description());
    }

    public <T> SettingOption<T> getOptionByValue(T value) {
        Optional<SettingOption<?>> option = this.autocompleteOptions.stream()
                .filter(o -> o.value().equals(value))
                .findFirst();

        if (option.isEmpty()) {
            throw new IllegalArgumentException("No option found for label: " + label);
        }

        return new SettingOption<>(option.get().label(), value, option.get().description());
    }
}


