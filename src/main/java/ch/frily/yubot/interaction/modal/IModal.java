package ch.frily.yubot.interaction.modal;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IModal {

    String getTitle();

    /**
     * Component tree with Label & component
     * @return
     */
    List<ModalTopLevelComponent> getComponents();

    void execute(@NotNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException;
}
