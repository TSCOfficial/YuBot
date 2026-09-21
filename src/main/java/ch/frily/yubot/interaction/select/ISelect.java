package ch.frily.yubot.interaction.select;

import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;

import java.util.List;

public interface ISelect {
    int MAX_SELECT_OPTIONS = 25;

    SelectMenu build();

    String getId();

    String getPlaceholder();

    default Integer getMinValues(){
        return 1;
    };

    default Integer getMaxValues(){
        return 1;
    };

    List<SelectOption> getOptions();

    default List<SelectOption> getDefaultOptions() {
        return List.of();
    };
}
