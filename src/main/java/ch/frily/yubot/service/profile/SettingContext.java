package ch.frily.yubot.service.profile;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;

/**
 *
 * @param setting the setting to modify the value from
 * @param event the slashcommand event
 * @param value the value as a String. To use the value for different datatypes, cast it like {@link Boolean#valueOf(String)}
 * @param <T>
 */
public record SettingContext<T>(
        Setting setting,
        SlashCommandInteractionEvent event,
        @Nullable String value
) {
    public SettingContext(
            Setting setting,
            SlashCommandInteractionEvent event) {
        this(setting, event, null);
    }
}
