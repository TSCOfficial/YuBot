package ch.frily.yubot.interaction.modal;

import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public interface IModal {

    String getTitle();

    /**
     * Component tree with Label & component
     * @return a list of the {@link ModalTopLevelComponent} children
     */
    List<ModalTopLevelComponent> getComponents();

    void execute(@NotNull ModalInteractionEvent event) throws SQLException, NullPointerException;
}
