package ch.frily.yubot.interaction.select;

import net.dv8tion.jda.api.components.selections.SelectOption;

import java.util.List;

public interface ISelect {

    String getId();

    String getPlaceholder();

    default Integer getMinValues(){
        return null;
    };

    default Integer getMaxValues(){
        return null;
    };

    List<SelectOption> getOptions();
}
