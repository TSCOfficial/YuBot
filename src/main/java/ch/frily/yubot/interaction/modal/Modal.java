package ch.frily.yubot.interaction.modal;

import ch.frily.yubot.interaction.ArgumentComponent;

import java.sql.SQLException;

public abstract class Modal extends ArgumentComponent implements IModal {
    public net.dv8tion.jda.api.modals.Modal build() throws SQLException {

        net.dv8tion.jda.api.modals.Modal.Builder modalBuilder = net.dv8tion.jda.api.modals.Modal.create(getFullIdentification(), getTitle());
        modalBuilder.addComponents(getComponents());
        return modalBuilder.build();
    }
}
