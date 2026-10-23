package ch.frily.yubot.service.profile;

import ch.frily.yubot.database.Table;
import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.service.profile.modal.ProfileSettingActivemodColorModal;
import ch.frily.yubot.util.EnvKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public enum Setting {
    ACTIVEMOD_SEND_IN_DM(
            "aktivitätsbestätigungsanfrage",
            "Entscheide wo deine ActiveMod-Nachrichten gesendet werden.",
            Table.SettingColumn.ACTIVEMOD_SEND_IN_DM,
            List.of(EnvKey.ROLE_MODERATOR),
            new OptionData(OptionType.STRING, "via", "Wähle das Medium aus, worüber die Nachricht versendet werden soll", true, true),
            List.of(
                    new SettingChoice<>("Server", false, "Sendet die Nachricht in #active-moderation (standard)"),
                    new SettingChoice<>("DM", true, "Sendet die Nachrichten per DM")
            ),
            (ctx) -> {
                try {

                    SettingRepository.upsertSetting(ctx.event().getMember(), ctx.setting(), Boolean.valueOf(ctx.value()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
    ),
    ABSENCE_NOTICE(
            "abwesenheitsmeldung",
            "Passe die Nachricht an, welche bei @Erwähnungen während deiner Absenz gesendet wird.",
            Table.SettingColumn.ABSENCE_NOTICE,
            List.of(EnvKey.ROLE_YUTEAM, EnvKey.ROLE_TWITCHMOD),
            new OptionData(OptionType.STRING, "text", "Wähle den Text der gesendet werden soll", true),
            (ctx) -> {
                try {
                    SettingRepository.upsertSetting(ctx.event().getMember(), ctx.setting(), ctx.event().getOption("text").getAsString());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
    ),
    ACTIVEMOD_GRAPH_COLOR(
            "activemod-farbe",
            "Passe die Farbe des Balkens deiner ActiveMod-Statistik an.",
            Table.SettingColumn.ACTIVEMOD_COLOR,
            List.of(EnvKey.ROLE_MODERATOR),
            (ctx) -> {
                ctx.event().replyModal(new ProfileSettingActivemodColorModal().build()).queue();
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
    final Consumer<SettingContext> onSubmit;

        /**
         * Define a Setting without options
         * @param label the label of the setting
         * @param description the description of the setting
         * @param dbColumn database column to be able to resolve for generic actions
         * @param <T> the datatype of the given setting
         */
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, Consumer<SettingContext> onSubmit){
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
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, OptionData option, Consumer<SettingContext> onSubmit){
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
    <T> Setting(String label, String description, Table.Column dbColumn, List<EnvKey> allowedRoles, OptionData option, List<SettingChoice> choices, Consumer<SettingContext> onSubmit){
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

    /**
     * Get the {@link SettingChoice} by {@link SettingChoice#label()}
     * @param label The laben of the SettingChoice
     * @return
     */
    public SettingChoice getChoiceByLabel(String label){
        log.info("Getting Choice by Label: {}", label);
        return this.autocompleteChoices.stream().filter(choice -> {
            log.info("checking label: {}", choice.label());
            return String.valueOf(choice.label()).equals(label);
        }).findFirst().orElse(null);
    }

    /**
     * Get the {@link SettingChoice} by {@link SettingChoice#value()}
     * @param value The selected value
     * @return
     */
    public <T> SettingChoice<T> getChoiceByValue(T value){
        log.info("Getting Choice by Value: '{}' with type {}", value, value.getClass().getSimpleName());
        return this.autocompleteChoices.stream().filter(choice -> {
            log.info("checking value: '{}' with type {}", choice.value(), choice.value().getClass().getSimpleName());
            return choice.value().equals(String.valueOf(value));
        }).findFirst().orElse(null);
    }
}


