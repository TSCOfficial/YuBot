package ch.frily.yubot.service.profile;

import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.Command;

import javax.annotation.Nullable;

/**
 * Provides choices/options for the settings
 * <p>
 *     This record is a medium between {@link Command.Choice} & {@link net.dv8tion.jda.api.components.selections.SelectOption}
 * </p>
 * @param label the label to display to the user
 * @param description an optional description that can be displayed, if possible
 * @param value the value that is stored to the database
 */
public record SettingChoice<T>(String label, T value, @Nullable String description, @Nullable Emoji emoji){

    public SettingChoice(String label, T value, @Nullable String description){
        this(label, value, description, null);
    }

    public SettingChoice(String label, T value, Emoji emoji){
        this(label, value, null, emoji);
    }

    public SettingChoice(String label, T value){
        this(label, value, null, null);
    }

    /**
     * Mutates the setting choice to a {@link Command.Choice}}
     * @return
     */
    public Command.Choice toCommandChoice() {
        return new Command.Choice(label, String.valueOf(value));
    }

    public SelectOption toSelectOption() {
        SelectOption option = SelectOption.of(label, String.valueOf(value));
        if (emoji != null) {
            option = option.withEmoji(emoji);
        }
        return option;
    }
}
