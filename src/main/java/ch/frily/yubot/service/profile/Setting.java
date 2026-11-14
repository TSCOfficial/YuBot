package ch.frily.yubot.service.profile;

import ch.frily.yubot.Client;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.service.profile.repository.SettingRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.profile.modal.ProfileSettingActivemodColorModal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public enum Setting {
    ACTIVEMOD_SEND_IN_DM(
            "aktivitätsbestätigungsanfrage",
            "Entscheide wo deine ActiveMod-Nachrichten gesendet werden.",
            Table.SettingColumn.ACTIVEMOD_SEND_IN_DM,
            List.of(Client.EnvKey.ROLE_MODERATOR),
            Boolean.class,
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
            List.of(Client.EnvKey.ROLE_YUTEAM, Client.EnvKey.ROLE_TWITCHMOD),
            String.class,
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
            "activemod-color",
            "Passe die Farbe des Balkens deiner ActiveMod-Statistik an.",
            Table.SettingColumn.ACTIVEMOD_COLOR,
            List.of(Client.EnvKey.ROLE_MODERATOR),
            String.class,
            List.of(
                    new SettingChoice<>("Blau / Petrol", Client.EnvKey.COLOR_BLAUPETROL.name(), Client.EnvKey.EMOJI_COLOR_BLAUPETROL.getAsEmoji()),
                    new SettingChoice<>("Blau / Kornblume", Client.EnvKey.COLOR_BLAUKORNBLUME.name(), Client.EnvKey.EMOJI_COLOR_BLAUKORNBLUME.getAsEmoji()),
                    new SettingChoice<>("Blau / Azur", Client.EnvKey.COLOR_BLAUAZUR.name(), Client.EnvKey.EMOJI_COLOR_BLAUAZUR.getAsEmoji()),
                    new SettingChoice<>("Blau / Aqua", Client.EnvKey.COLOR_BLAUAQUA.name(), Client.EnvKey.EMOJI_COLOR_BLAUAQUA.getAsEmoji()),

                    new SettingChoice<>("Gelb / Zitrone", Client.EnvKey.COLOR_GELBZITRONE.name(), Client.EnvKey.EMOJI_COLOR_GELBZITRONE.getAsEmoji()),
                    new SettingChoice<>("Gelb / Vanille", Client.EnvKey.COLOR_GELBVANILLE.name(), Client.EnvKey.EMOJI_COLOR_GELBVANILLE.getAsEmoji()),
                    new SettingChoice<>("Gelb / Senf", Client.EnvKey.COLOR_GELBSENF.name(), Client.EnvKey.EMOJI_COLOR_GELBSENF.getAsEmoji()),
                    new SettingChoice<>("Gelb / Gold", Client.EnvKey.COLOR_GELBGOLD.name(), Client.EnvKey.EMOJI_COLOR_GELBGOLD.getAsEmoji()),

                    new SettingChoice<>("Grün / Smaragd", Client.EnvKey.COLOR_GRUENSMARAGD.name(), Client.EnvKey.EMOJI_COLOR_GRUENSMARAGD.getAsEmoji()),
                    new SettingChoice<>("Grün / Olivgrün", Client.EnvKey.COLOR_GRUENOLIVGRUEN.name(), Client.EnvKey.EMOJI_COLOR_GRUENOLIVGRUEN.getAsEmoji()),
                    new SettingChoice<>("Grün / Lindgrün", Client.EnvKey.COLOR_GRUENLINDGRUEN.name(), Client.EnvKey.EMOJI_COLOR_GRUENLINDGRUEN.getAsEmoji()),
                    new SettingChoice<>("Grün / Kleegrün", Client.EnvKey.COLOR_GRUENKLEEGRUEN.name(), Client.EnvKey.EMOJI_COLOR_GRUENKLEEGRUEN.getAsEmoji()),

                    new SettingChoice<>("Pink / Rosa", Client.EnvKey.COLOR_PINKROSA.name(), Client.EnvKey.EMOJI_COLOR_PINKROSA.getAsEmoji()),
                    new SettingChoice<>("Pink / Pink", Client.EnvKey.COLOR_PINKPINK.name(), Client.EnvKey.EMOJI_COLOR_PINKPINK.getAsEmoji()),
                    new SettingChoice<>("Pink / Fuchsia", Client.EnvKey.COLOR_PINKFUCHSIA.name(), Client.EnvKey.EMOJI_COLOR_PINKFUCHSIA.getAsEmoji()),
                    new SettingChoice<>("Pink / Flamingo", Client.EnvKey.COLOR_PINKFLAMINGO.name(), Client.EnvKey.EMOJI_COLOR_PINKFLAMINGO.getAsEmoji()),

                    new SettingChoice<>("Rot / Scharlach", Client.EnvKey.COLOR_ROTSCHARLACH.name(), Client.EnvKey.EMOJI_COLOR_ROTSCHARLACH.getAsEmoji()),
                    new SettingChoice<>("Rot / Mandarine", Client.EnvKey.COLOR_ROTMANDARINE.name(), Client.EnvKey.EMOJI_COLOR_ROTMANDARINE.getAsEmoji()),
                    new SettingChoice<>("Rot / Kupfer", Client.EnvKey.COLOR_ROTKUPFER.name(), Client.EnvKey.EMOJI_COLOR_ROTKUPFER.getAsEmoji()),
                    new SettingChoice<>("Rot / Koralle", Client.EnvKey.COLOR_ROTKORALLE.name(), Client.EnvKey.EMOJI_COLOR_ROTKORALLE.getAsEmoji()),
                    new SettingChoice<>("Rot / Aprikose", Client.EnvKey.COLOR_ROTAPRIKOSE.name(), Client.EnvKey.EMOJI_COLOR_ROTAPRIKOSE.getAsEmoji()),

                    new SettingChoice<>("Violett / Veilchen", Client.EnvKey.COLOR_VIOLETTVEILCHEN.name(), Client.EnvKey.EMOJI_COLOR_VIOLETTVEILCHEN.getAsEmoji()),
                    new SettingChoice<>("Violett / Orchidee", Client.EnvKey.COLOR_VIOLETTORCHIDEE.name(), Client.EnvKey.EMOJI_COLOR_VIOLETTORCHIDEE.getAsEmoji()),
                    new SettingChoice<>("Violett / Lavendel", Client.EnvKey.COLOR_VIOLETTLAVENDER.name(), Client.EnvKey.EMOJI_COLOR_VIOLETTLAVENDER.getAsEmoji()),
                    new SettingChoice<>("Violett / Brombeere", Client.EnvKey.COLOR_VIOLETTBROMBEERE.name(), Client.EnvKey.EMOJI_COLOR_VIOLETTBROMBEERE.getAsEmoji())
            ),
            (ctx) -> {
                try {
                    ctx.event().replyModal(new ProfileSettingActivemodColorModal().build()).queue();
                } catch (Exception e) {
                    ExceptionHandler.handle(e, ctx.event());
                }
            },
            (ctx) -> {
                SettingChoice choice = ctx.setting().getChoiceByValue(ctx.value());
                return String.format("%s %s", choice.emoji(), choice.label());
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
    @Getter
    final Class<?> dataType;

    /** Input-Option - only one per setting */
    @Getter
    OptionData option;

    /** Autocomplete choices for given option */
    @Getter
    final List<SettingChoice> choices = new ArrayList<>();
    /** Function to execute when the setting is executed */
    @Getter
    final Consumer<SettingOnSubmitCtx> onSubmit;
    /** Function that maps the given setting to a human-readable string */
    @Getter
    Function<SettingOnDisplayCtx, String> onDisplay;

    /**
     * Define a Setting without options
     * @param label the label of the setting
     * @param description the description of the setting
     * @param dbColumn database column to be able to resolve for generic actions
     * @param <T> the datatype of the given setting
     */
    <T> Setting(String label, String description, Table.Column dbColumn, List<Client.EnvKey> allowedRoles, Class<T> dataType, Consumer<SettingOnSubmitCtx> onSubmit, Function<SettingOnDisplayCtx, String> onDisplay) {
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.allowedRoles = allowedRoles.stream().map(Client.EnvKey::getAsRole).toList();
        this.dataType = dataType;
        this.onSubmit = onSubmit;
        this.onDisplay = onDisplay;
    }

    /**
     * Define a Setting without options, that provide detached options/choices
     * <p>
     *     This is used for settings that require an second interaction (such as a Modal), in which the choices/options are then displayed, instead of in the slashcommand-option.
     * </p>
     * @param label the label of the setting
     * @param description the description of the setting
     * @param dbColumn database column to be able to resolve for generic actions
     * @param <T> the datatype of the given setting
     */
    <T> Setting(String label, String description, Table.Column dbColumn, List<Client.EnvKey> allowedRoles, Class<T> dataType, List<SettingChoice> detachedChoices, Consumer<SettingOnSubmitCtx> onSubmit, Function<SettingOnDisplayCtx, String> onDisplay) {
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.allowedRoles = allowedRoles.stream().map(Client.EnvKey::getAsRole).toList();
        this.dataType = dataType;
        this.choices.addAll(detachedChoices);
        this.onSubmit = onSubmit;
        this.onDisplay = onDisplay;
    }

    /**
     * Define a Setting with options but no autocomplete (free-text)
     * @param label the label of the setting
     * @param description the description of the setting
     * @param dbColumn database column to be able to resolve for generic actions
     * @param <T> the datatype of the given setting
     */
    <T> Setting(String label, String description, Table.Column dbColumn, List<Client.EnvKey> allowedRoles, Class<T> dataType, OptionData option, Consumer<SettingOnSubmitCtx> onSubmit){
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.allowedRoles = allowedRoles.stream().map(Client.EnvKey::getAsRole).toList();
        this.dataType = dataType;
        this.option = option;
        this.onSubmit = onSubmit;
    }

    /**
     * Define a Setting with autocomplete choices
     * @param label the label of the setting
     * @param description the description of the setting
     * @param dbColumn database column to be able to resolve for generic actions
     * @param choices Autocomplete choices for that setting
     * @param <T> the datatype of the setting
     */
    <T> Setting(String label, String description, Table.Column dbColumn, List<Client.EnvKey> allowedRoles, Class<T> dataType, OptionData option, List<SettingChoice> choices, Consumer<SettingOnSubmitCtx> onSubmit){
        this.label = label;
        this.description = description;
        this.dbColumn = dbColumn;
        this.allowedRoles = allowedRoles.stream().map(Client.EnvKey::getAsRole).toList();
        this.dataType = dataType;
        this.option = option;
        this.choices.addAll(choices);
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
        return this.choices.stream().filter(choice -> {
            return String.valueOf(choice.label()).equals(label);
        }).findFirst().orElse(null);
    }

    /**
     * Get the {@link SettingChoice} by {@link SettingChoice#value()}
     * @param value The selected value
     * @return
     */
    public <T> SettingChoice<T> getChoiceByValue(T value){
        return this.choices.stream().filter(choice -> {
            return String.valueOf(choice.value()).equals(String.valueOf(value));
        }).findFirst().orElse(null);
    }
}


