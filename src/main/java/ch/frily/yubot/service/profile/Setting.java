package ch.frily.yubot.service.profile;

import ch.frily.yubot.database.Table;
import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.service.profile.modal.ProfileSettingActivemodColorModal;
import ch.frily.yubot.util.EnvKey;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public enum Setting {
    ACTIVEMOD_SEND_IN_DM(
            "aktivitätsbestätigungsanfrage",
            "Entscheide wo deine ActiveMod-Nachrichten gesendet werden.",
            Table.SettingColumn.ACTIVEMOD_SEND_IN_DM,
            List.of(EnvKey.ROLE_MODERATOR),
            new OptionData(OptionType.STRING, "via", "Wähle das Medium aus, worüber die Nachricht versendet werden soll"),
            List.of(
                    new SettingChoice<>("Server", false, "Sendet die Nachricht in #active-moderation (standard)"),
                    new SettingChoice<>("DM", true, "Sendet die Nachrichten per DM")
            ),
            (ctx) -> {
                try {
                    SettingRepository.upsertSetting(ctx.event().getMember(), ctx.setting(), ctx.event().getOption("via").getAsString());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                return "setting gespeichert";
            }
    ),
    ABSENCE_NOTICE(
            "abwesenheitsmeldung",
            "Nachricht welche bei @Erwähnungen während deiner Absenz gesendet wird.",
            Table.SettingColumn.ABSENCE_NOTICE,
            List.of(EnvKey.ROLE_YUTEAM, EnvKey.ROLE_TWITCHMOD),
            new OptionData(OptionType.STRING, "text", "Wähle den Text der gesendet werden soll"),
            (ctx) -> {
                try {
                    SettingRepository.upsertSetting(ctx.event().getMember(), ctx.setting(), ctx.event().getOption("text").getAsString());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                return "setting gespeichert";
            }
    ),
    ACTIVEMOD_GRAPH_COLOR(
            "activemod-farbe",
            "Passe die Farbe des Balkens deiner ActiveMod-Statistik an.",
            Table.SettingColumn.ACTIVEMOD_COLOR,
            List.of(EnvKey.ROLE_MODERATOR),
            (ctx) -> {
                ctx.event().replyModal(new ProfileSettingActivemodColorModal().build()).queue();
                return "modal gesendet";
            }
    );

    @Getter
    final String label;
    @Getter
    final String description;
    @Getter
    final Table.Column dbColumn;
    @Getter
    final List<Role> allowedRoles;

    /** Input-Option - only one per setting */
    @Getter
    OptionData option;

    /** Autocomplete choices for given option */
    @Getter
    final List<SettingChoice> autocompleteChoices = new ArrayList<>();
    /** Minimal allowed characters for a string*/
    @Getter
    final Function<SettingContext, String> onSubmit;

        /**
         * Define a Setting without options
         * @param label the label of the setting
         * @param description the description of the setting
         * @param dbColumn database column to be able to resolve for generic actions
         * @param <T> the datatype of the given setting
         */
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, Function<SettingContext, String> onSubmit){
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.allowedRoles = allowedRoles.stream().map(EnvKey::getAsRole).toList();
        this.onSubmit = onSubmit;
    }

    /**
     * Define a Setting with options but no autocomplete (free-text)
     * @param label the label of the setting
     * @param description the description of the setting
     * @param dbColumn database column to be able to resolve for generic actions
     * @param <T> the datatype of the given setting
     */
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, OptionData option, Function<SettingContext, String> onSubmit){
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.allowedRoles = allowedRoles.stream().map(EnvKey::getAsRole).toList();
        this.option = option;
        this.onSubmit = onSubmit;
    }

    /**
     * Define a Setting with autocomplete options
     * @param label the label of the setting
     * @param description the description of the setting
     * @param dbColumn database column to be able to resolve for generic actions
     * @param choices Autocomplete choices for that setting
     * @param <T> the datatype of the setting
     */
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, OptionData option, List<SettingChoice> choices, Function<SettingContext, String> onSubmit){
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.allowedRoles = allowedRoles.stream().map(EnvKey::getAsRole).toList();
        this.option = option;
        this.autocompleteChoices.addAll(choices);
        this.onSubmit = onSubmit;
    }

    public static Setting getSettingByLabel(String label){
        return Arrays.stream(Setting.values()).filter(setting -> setting.getLabel().equals(label)).findFirst().orElse(null);
    }

    public SettingChoice getChoiceByValue(Object value){
        return this.autocompleteChoices.stream().filter(choice -> String.valueOf(choice.value()).equals(value)).findFirst().orElse(null);
    }
}


