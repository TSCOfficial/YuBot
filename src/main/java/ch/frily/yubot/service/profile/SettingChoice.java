package ch.frily.yubot.service.profile;

import net.dv8tion.jda.api.interactions.commands.Command;

import javax.annotation.Nullable;

/**
 * Maps the settings to all required generic utilities
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
