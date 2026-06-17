package ch.frily.yubot.interaction.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ISlashCommand {

    /**
     * Get the name of a slashcommand.
     *
     * @return a {@link String}
     */
    String getName();


    /**
     * Get the description of a slashcommand.
     *
     * @return a {@link String}
     */
    String getDescription();

    /**
     * Execute a slashcommand by its interaction event.<br>
     * @param event The {@link SlashCommandInteractionEvent}
     */
    void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException;

    /**
     * Get the options and choices of a slashcommand.
     *
     * @return a {@link List} of {@link OptionData}.
     */
    default List<OptionData> getOptions() {
        return List.of();
    };

    /**
     * Get the autocompletion of a slashcommand.
     * @return a {@link Map} of a {@link String} & a {@link List}.
     */
    default Map<String, List<?>> getAutocomplete() {
        return Map.of();
    };

    /**
     * Get the default permissions of a slashcommand
     * @return a list of {@link Permission}
     */
    default List<Permission> getDefaultPermissions() {
        return List.of();
    }
}
