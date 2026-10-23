package ch.frily.yubot.service.profile.modal;

import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.profile.SettingChoice;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

public class ProfileSettingActivemodColorModal extends Modal {
    @Override
    public String getId() {
        return "profile-setting-activemod-color";
    }

    @Override
    public String getTitle() {
        return "Statistikfarbe ändern";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {
        StringSelectMenu.Builder selectBuilder = StringSelectMenu.create("activemod-color-select");
        selectBuilder.setMinValues(1);
        selectBuilder.setMaxValues(1);
        selectBuilder.setRequired(true);
        selectBuilder.setPlaceholder("Farbe auswählen");
        selectBuilder.addOptions(getOptions());

        return List.of(
            Label.of(
                    "Activemod Statistikfarbe",
                    selectBuilder.build()
            )
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {

    }
    
    private List<SelectOption> getOptions() {
        return List.of(
                SelectOption.of("Blau / Petrol", EnvKey.EMOJI_COLOR_BLAUPETROL.name()),
                SelectOption.of("Blau / Kornblume", EnvKey.EMOJI_COLOR_BLAUKORNBLUME.name()),
                SelectOption.of("Blau / Azur", EnvKey.EMOJI_COLOR_BLAUAZUR.name()),
                SelectOption.of("Blau / Aqua", EnvKey.EMOJI_COLOR_BLAUAQUA.name()),

                SelectOption.of("Gelb / Zitrone", EnvKey.EMOJI_COLOR_GELBZITRONE.name()),
                SelectOption.of("Gelb / Vanille", EnvKey.EMOJI_COLOR_GELBVANILLE.name()),
                SelectOption.of("Gelb / Senf", EnvKey.EMOJI_COLOR_GELBSENF.name()),
                SelectOption.of("Gelb / Gold", EnvKey.EMOJI_COLOR_GELBGOLD.name()),

                SelectOption.of("Grün / Smaragd", EnvKey.EMOJI_COLOR_GRUENSMARAGD.name()),
                SelectOption.of("Grün / Olivgrün", EnvKey.EMOJI_COLOR_GRUENOLIVGRUEN.name()),
                SelectOption.of("Grün / Lindgrün", EnvKey.EMOJI_COLOR_GRUENLINDGRUEN.name()),
                SelectOption.of("Grün / Kleegrün", EnvKey.EMOJI_COLOR_GRUENKLEEGRUEN.name()),

                SelectOption.of("Pink / Rosa", EnvKey.EMOJI_COLOR_PINKROSA.name()),
                SelectOption.of("Pink / Pink", EnvKey.EMOJI_COLOR_PINKPINK.name()),
                SelectOption.of("Pink / Fuchsia", EnvKey.EMOJI_COLOR_PINKFUCHSIA.name()),
                SelectOption.of("Pink / Flamingo", EnvKey.EMOJI_COLOR_PINKFLAMINGO.name()),

                SelectOption.of("Rot / Scharlach", EnvKey.EMOJI_COLOR_ROTSCHARLACH.name()),
                SelectOption.of("Rot / Mandarine", EnvKey.EMOJI_COLOR_ROTMANDARINE.name()),
                SelectOption.of("Rot / Kupfer", EnvKey.EMOJI_COLOR_ROTKUPFER.name()),
                SelectOption.of("Rot / Koralle", EnvKey.EMOJI_COLOR_ROTKORALLE.name()),
                SelectOption.of("Rot / Aprikose", EnvKey.EMOJI_COLOR_ROTAPRIKOSE.name()),

                SelectOption.of("Violett / Veilchen", EnvKey.EMOJI_COLOR_VIOLETTVEILCHEN.name()),
                SelectOption.of("Violett / Orchidee", EnvKey.EMOJI_COLOR_VIOLETTORCHIDEE.name()),
                SelectOption.of("Violett / Lavendel", EnvKey.EMOJI_COLOR_VIOLETTLAVENDER.name()),
                SelectOption.of("Violett / Brombeere", EnvKey.EMOJI_COLOR_VIOLETTBROMBEERE.name())
        );
    }
}
