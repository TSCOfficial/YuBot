package ch.frily.yubot.service.profile;

import net.dv8tion.jda.api.interactions.commands.Command;

import javax.annotation.Nullable;

/**
 * Provides autocomplete choices for the settings
 * <p>
 *     While {@link #value} being the value that is stored to the database
 * </p>
 */
public record SettingChoice<T>(String label, T value, @Nullable String description){

    public SettingChoice(String label, T value){
        this(label, value, null);
    }

    /**
     * Mutates the setting choice to a {@link Command.Choice}}
     * @return
     */
    public Command.Choice toChoice() {
        return new Command.Choice(label, String.valueOf(value));
    }
}
