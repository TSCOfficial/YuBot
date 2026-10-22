package ch.frily.yubot.service.profile;

import ch.frily.yubot.database.Table;
import ch.frily.yubot.util.EnvKey;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum Setting {
    ACTIVEMOD_SEND_IN_DM(
            "aktivitätsbestätigungsanfrage",
            "Entscheide wo deine ActiveMod-Nachrichten gesendet werden.",
            Table.SettingColumn.ACTIVEMOD_SEND_IN_DM,
            List.of(EnvKey.ROLE_MODERATOR),
            Boolean.class,
            List.of(
                    new SettingOption<>("Via Server", false, "Sendet die Nachricht in #active-moderation (standard)"),
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
    ),
    ACTIVEMOD_GRAPH_COLOR(
            "activemod-farbe",
            "Passe die Farbe des Balkens deiner ActiveMod-Statistik an.",
            Table.SettingColumn.ACTIVEMOD_COLOR,
            List.of(EnvKey.ROLE_MODERATOR),
            String.class,
            List.of(
                    new SettingOption<>("Blau / Türkis", EnvKey.EMOJI_COLOR_BLAUTUERKIS.name()),
                    new SettingOption<>("Blau / Petrol", EnvKey.EMOJI_COLOR_BLAUPETROL.name()),
                    new SettingOption<>("Blau / Kornblume", EnvKey.EMOJI_COLOR_BLAUKORNBLUME.name()),
                    new SettingOption<>("Blau / Indigo", EnvKey.EMOJI_COLOR_BLAUINDIGO.name()),
                    new SettingOption<>("Blau / Eisblau", EnvKey.EMOJI_COLOR_BLAUEISBLAU.name()),
                    new SettingOption<>("Blau / Blau", EnvKey.EMOJI_COLOR_BLAUBLAU.name()),
                    new SettingOption<>("Blau / Azur", EnvKey.EMOJI_COLOR_BLAUAZUR.name()),
                    new SettingOption<>("Blau / Aqua", EnvKey.EMOJI_COLOR_BLAUAQUA.name()),

                    new SettingOption<>("Gelb / Zitrone", EnvKey.EMOJI_COLOR_GELBZITRONE.name()),
                    new SettingOption<>("Gelb / Vanille", EnvKey.EMOJI_COLOR_GELBVANILLE.name()),
                    new SettingOption<>("Gelb / Senf", EnvKey.EMOJI_COLOR_GELBSENF.name()),
                    new SettingOption<>("Gelb / Gold", EnvKey.EMOJI_COLOR_GELBGOLD.name()),
                    new SettingOption<>("Gelb / Gelb", EnvKey.EMOJI_COLOR_GELBGELB.name()),

                    new SettingOption<>("Grün / Smaragd", EnvKey.EMOJI_COLOR_GRUENSMARAGD.name()),
                    new SettingOption<>("Grün / Seegrün", EnvKey.EMOJI_COLOR_GRUENSEEGRUEN.name()),
                    new SettingOption<>("Grün / Pistazien", EnvKey.EMOJI_COLOR_GRUENPISTAZIE.name()),
                    new SettingOption<>("Grün / Olivgrün", EnvKey.EMOJI_COLOR_GRUENOLIVGRUEN.name()),
                    new SettingOption<>("Grün / Minze", EnvKey.EMOJI_COLOR_GRUENMINZE.name()),
                    new SettingOption<>("Grün / Lindgrün", EnvKey.EMOJI_COLOR_GRUENLINDGRUEN.name()),
                    new SettingOption<>("Grün / Kleegrün", EnvKey.EMOJI_COLOR_GRUENKLEEGRUEN.name()),
                    new SettingOption<>("Grün / Hellgrün", EnvKey.EMOJI_COLOR_GRUENHELLGRUEN.name()),
                    new SettingOption<>("Grün / Grün", EnvKey.EMOJI_COLOR_GRUENGRUEN.name()),

                    new SettingOption<>("Pink / Zartrosa", EnvKey.EMOJI_COLOR_PINKZARTROSA.name()),
                    new SettingOption<>("Pink / Rosequarz", EnvKey.EMOJI_COLOR_PINKROSEQUARZ.name()),
                    new SettingOption<>("Pink / Rosa", EnvKey.EMOJI_COLOR_PINKROSA.name()),
                    new SettingOption<>("Pink / Pink", EnvKey.EMOJI_COLOR_PINKPINK.name()),
                    new SettingOption<>("Pink / Magenta", EnvKey.EMOJI_COLOR_PINKMAGENTA.name()),
                    new SettingOption<>("Pink / Fuchsia", EnvKey.EMOJI_COLOR_PINKFUCHSIA.name()),
                    new SettingOption<>("Pink / Flamingo", EnvKey.EMOJI_COLOR_PINKFLAMINGO.name()),

                    new SettingOption<>("Rot / Scharlach", EnvKey.EMOJI_COLOR_ROTSCHARLACH.name()),
                    new SettingOption<>("Rot / Rotorange", EnvKey.EMOJI_COLOR_ROTROTORANGE.name()),
                    new SettingOption<>("Rot / Rot", EnvKey.EMOJI_COLOR_ROTROT.name()),
                    new SettingOption<>("Rot / Orange", EnvKey.EMOJI_COLOR_ROTORANGE.name()),
                    new SettingOption<>("Rot / Mandarine", EnvKey.EMOJI_COLOR_ROTMANDARINE.name()),
                    new SettingOption<>("Rot / Kupfer", EnvKey.EMOJI_COLOR_ROTKUPFER.name()),
                    new SettingOption<>("Rot / Koralle", EnvKey.EMOJI_COLOR_ROTKORALLE.name()),
                    new SettingOption<>("Rot / Aprikose", EnvKey.EMOJI_COLOR_ROTAPRIKOSE.name()),

                    new SettingOption<>("Violett / Violett", EnvKey.EMOJI_COLOR_VIOLETTVIOLETT.name()),
                    new SettingOption<>("Violett / Veilchen", EnvKey.EMOJI_COLOR_VIOLETTVEILCHEN.name()),
                    new SettingOption<>("Violett / Pflaume", EnvKey.EMOJI_COLOR_VIOLETTPFLAUME.name()),
                    new SettingOption<>("Violett / Orchidee", EnvKey.EMOJI_COLOR_VIOLETTORCHIDEE.name()),
                    new SettingOption<>("Violett / Lila", EnvKey.EMOJI_COLOR_VIOLETTLILA.name()),
                    new SettingOption<>("Violett / Lavendel", EnvKey.EMOJI_COLOR_VIOLETTLAVENDER.name()),
                    new SettingOption<>("Violett / Flieder", EnvKey.EMOJI_COLOR_VIOLETTFLIEDER.name()),
                    new SettingOption<>("Violett / Dunkelmagenta", EnvKey.EMOJI_COLOR_VIOLETTDUNKELMAGENTA.name()),
                    new SettingOption<>("Violett / Brombeere", EnvKey.EMOJI_COLOR_VIOLETTBROMBEERE.name())
            )
    );

    @Getter
    final String label;
    @Getter
    final String description;
    @Getter
    final Table.Column dbColumn;
    @Getter
    final Class<?> dataType;
    @Getter
    final List<Role> allowedRoles;
    @Getter
    final List<SettingOption> autocompleteOptions = new ArrayList<>();
    /** Minimal allowed characters for a string*/
    @Getter
    int minLength;
    /** Maximal allowed characters for a string*/
    @Getter
    int maxLength;

    /**
     * Define a Setting without autocomplete options
     * <p>
     *     This is primarily used for boolean-settings, because boolean inputs automatically show true/false, or for free-text input.
     * </p>
     * @param label the label of the setting
     * @param description the description of the setting
     * @param dbColumn database column to be able to resolve for generic actions
     * @param dataType The type of the setting
     * @param <T> the datatype of the given setting
     */
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, Class<T> dataType, int minLength, int maxLength){
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.dataType = dataType;
        this.allowedRoles = allowedRoles.stream().map(EnvKey::getAsRole).toList();
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    /**
     * Define a Setting with autocomplete options
     * @param label the label of the setting
     * @param description the description of the setting
     * @param dbColumn database column to be able to resolve for generic actions
     * @param dataType The type of the setting
     * @param options Selectable types for that setting in the given dataType
     * @param <T> the datatype of the setting
     */
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, Class<T> dataType, List<SettingOption> options){
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.dataType = dataType;
        this.allowedRoles = allowedRoles.stream().map(EnvKey::getAsRole).toList();
        this.autocompleteOptions.addAll(options);
    }

    public static Setting getSettingByLabel(String label){
        return Arrays.stream(Setting.values()).filter(setting -> setting.getLabel().equals(label)).findFirst().orElse(null);
    }

    /**
     * Get the option by its option-label
     * <p>
     *     This use primarily used for the discord autocomplete feature.
     *     The autocomplete shows the label and needs to be converted to its corresponding value for the database.
     * </p>
     * @param label the label of the setting to get the value from
     * @param dataType the datatype of the setting
     * @return the value of the setting
     * @param <T> the datatype of the value
     */
    public <T> SettingOption<T> getOptionByLabel(String label, Class<T> dataType) {
        Optional<SettingOption> option = this.autocompleteOptions.stream()
                .filter(o -> o.label().equals(label))
                .findFirst();

        if (option.isEmpty()) {
            throw new IllegalArgumentException("No option found for label: " + label);
        }

        T value = dataType.cast(option.get().value());
        return new SettingOption<>(option.get().label(), value, option.get().description());
    }

    public <T> SettingOption<T> getOptionByValue(T value) {
        Optional<SettingOption> option = this.autocompleteOptions.stream()
                .filter(o -> o.value().equals(value))
                .findFirst();

        if (option.isEmpty()) {
            throw new IllegalArgumentException("No option found for label: " + label);
        }

        return new SettingOption<>(option.get().label(), value, option.get().description());
    }
}


