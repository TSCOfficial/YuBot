package ch.frily.yubot.service.profile;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nullable;

/**
 * The context carrying the data for a setting change submit
 * @param setting the setting to modify the value from
 * @param event the slashcommand event
 * @param value the value as a String. To use the value for different datatypes, cast it like {@link Boolean#valueOf(String)}
 * @param <T>
 */
public record SettingOnSubmitCtx<T>(
        Setting setting,
        SlashCommandInteractionEvent event,
        @Nullable String value
) {
    public SettingOnSubmitCtx(
            Setting setting,
            SlashCommandInteractionEvent event) {
        this(setting, event, null);
    }
}
