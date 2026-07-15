package ch.frily.yubot.interaction.select;

import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface IStringSelect extends ISelect {

    default StringSelectMenu build() {
        return StringSelectMenu.create(getId())
                .setPlaceholder(getPlaceholder())
                .addOptions(getOptions())
                .setMinValues(getMinValues())
                .setMaxValues(getMaxValues())
                .build();
    }

    void execute(@NotNull StringSelectInteractionEvent event) throws SQLException, ClassNotFoundException;
}
