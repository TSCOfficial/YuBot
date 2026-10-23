package ch.frily.yubot.service.profile;

import ch.frily.yubot.database.Table;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The context carrying the data for displaying a setting
 * <p>
 *     Use this context to be able to display settings with detached choices or settings without choices
 * </p>
 * @param setting the setting to modify the value from
 * @param value the value as a String. To use the value for different datatypes, cast it like {@link Boolean#valueOf(String)}
 */
public record SettingOnDisplayCtx(
        Setting setting,
        String value
) {
}
