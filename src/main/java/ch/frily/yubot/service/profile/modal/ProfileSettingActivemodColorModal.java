package ch.frily.yubot.service.profile.modal;

import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.profile.Setting;
import ch.frily.yubot.service.profile.SettingChoice;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

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
        String colorValue = Objects.requireNonNull(event.getValue("activemod-color-select")).getAsStringList().getFirst();

        Setting setting = Setting.ACTIVEMOD_GRAPH_COLOR;
        SettingRepository.upsertSetting(event.getMember(), setting, colorValue);
        SelectOption selectedOption = getOptions().stream().filter(option -> Objects.equals(option.getValue(), colorValue)).findFirst().orElseThrow();
        String replyMessage = String.format("✅ Die Einstellung **%s** wurde auf %s `%s` festgelegt.", setting.getLabel(), selectedOption.getEmoji(), selectedOption.getLabel());
        event.reply(replyMessage).setEphemeral(true).queue();
    }
    
    private List<SelectOption> getOptions() {
        return List.of(
                SelectOption.of("Blau / Petrol", EnvKey.EMOJI_COLOR_BLAUPETROL.name()).withEmoji(EnvKey.EMOJI_COLOR_BLAUPETROL.getAsEmoji()),
                SelectOption.of("Blau / Kornblume", EnvKey.EMOJI_COLOR_BLAUKORNBLUME.name()).withEmoji(EnvKey.EMOJI_COLOR_BLAUKORNBLUME.getAsEmoji()),
                SelectOption.of("Blau / Azur", EnvKey.EMOJI_COLOR_BLAUAZUR.name()).withEmoji(EnvKey.EMOJI_COLOR_BLAUAZUR.getAsEmoji()),
                SelectOption.of("Blau / Aqua", EnvKey.EMOJI_COLOR_BLAUAQUA.name()).withEmoji(EnvKey.EMOJI_COLOR_BLAUAQUA.getAsEmoji()),

                SelectOption.of("Gelb / Zitrone", EnvKey.EMOJI_COLOR_GELBZITRONE.name()).withEmoji(EnvKey.EMOJI_COLOR_GELBZITRONE.getAsEmoji()),
                SelectOption.of("Gelb / Vanille", EnvKey.EMOJI_COLOR_GELBVANILLE.name()).withEmoji(EnvKey.EMOJI_COLOR_GELBVANILLE.getAsEmoji()),
                SelectOption.of("Gelb / Senf", EnvKey.EMOJI_COLOR_GELBSENF.name()).withEmoji(EnvKey.EMOJI_COLOR_GELBSENF.getAsEmoji()),
                SelectOption.of("Gelb / Gold", EnvKey.EMOJI_COLOR_GELBGOLD.name()).withEmoji(EnvKey.EMOJI_COLOR_GELBGOLD.getAsEmoji()),

                SelectOption.of("Grün / Smaragd", EnvKey.EMOJI_COLOR_GRUENSMARAGD.name()).withEmoji(EnvKey.EMOJI_COLOR_GRUENSMARAGD.getAsEmoji()),
                SelectOption.of("Grün / Olivgrün", EnvKey.EMOJI_COLOR_GRUENOLIVGRUEN.name()).withEmoji(EnvKey.EMOJI_COLOR_GRUENOLIVGRUEN.getAsEmoji()),
                SelectOption.of("Grün / Lindgrün", EnvKey.EMOJI_COLOR_GRUENLINDGRUEN.name()).withEmoji(EnvKey.EMOJI_COLOR_GRUENLINDGRUEN.getAsEmoji()),
                SelectOption.of("Grün / Kleegrün", EnvKey.EMOJI_COLOR_GRUENKLEEGRUEN.name()).withEmoji(EnvKey.EMOJI_COLOR_GRUENKLEEGRUEN.getAsEmoji()),

                SelectOption.of("Pink / Rosa", EnvKey.EMOJI_COLOR_PINKROSA.name()).withEmoji(EnvKey.EMOJI_COLOR_PINKROSA.getAsEmoji()),
                SelectOption.of("Pink / Pink", EnvKey.EMOJI_COLOR_PINKPINK.name()).withEmoji(EnvKey.EMOJI_COLOR_PINKPINK.getAsEmoji()),
                SelectOption.of("Pink / Fuchsia", EnvKey.EMOJI_COLOR_PINKFUCHSIA.name()).withEmoji(EnvKey.EMOJI_COLOR_PINKFUCHSIA.getAsEmoji()),
                SelectOption.of("Pink / Flamingo", EnvKey.EMOJI_COLOR_PINKFLAMINGO.name()).withEmoji(EnvKey.EMOJI_COLOR_PINKFLAMINGO.getAsEmoji()),

                SelectOption.of("Rot / Scharlach", EnvKey.EMOJI_COLOR_ROTSCHARLACH.name()).withEmoji(EnvKey.EMOJI_COLOR_ROTSCHARLACH.getAsEmoji()),
                SelectOption.of("Rot / Mandarine", EnvKey.EMOJI_COLOR_ROTMANDARINE.name()).withEmoji(EnvKey.EMOJI_COLOR_ROTMANDARINE.getAsEmoji()),
                SelectOption.of("Rot / Kupfer", EnvKey.EMOJI_COLOR_ROTKUPFER.name()).withEmoji(EnvKey.EMOJI_COLOR_ROTKUPFER.getAsEmoji()),
                SelectOption.of("Rot / Koralle", EnvKey.EMOJI_COLOR_ROTKORALLE.name()).withEmoji(EnvKey.EMOJI_COLOR_ROTKORALLE.getAsEmoji()),
                SelectOption.of("Rot / Aprikose", EnvKey.EMOJI_COLOR_ROTAPRIKOSE.name()).withEmoji(EnvKey.EMOJI_COLOR_ROTAPRIKOSE.getAsEmoji()),

                SelectOption.of("Violett / Veilchen", EnvKey.EMOJI_COLOR_VIOLETTVEILCHEN.name()).withEmoji(EnvKey.EMOJI_COLOR_VIOLETTVEILCHEN.getAsEmoji()),
                SelectOption.of("Violett / Orchidee", EnvKey.EMOJI_COLOR_VIOLETTORCHIDEE.name()).withEmoji(EnvKey.EMOJI_COLOR_VIOLETTORCHIDEE.getAsEmoji()),
                SelectOption.of("Violett / Lavendel", EnvKey.EMOJI_COLOR_VIOLETTLAVENDER.name()).withEmoji(EnvKey.EMOJI_COLOR_VIOLETTLAVENDER.getAsEmoji()),
                SelectOption.of("Violett / Brombeere", EnvKey.EMOJI_COLOR_VIOLETTBROMBEERE.name()).withEmoji(EnvKey.EMOJI_COLOR_VIOLETTBROMBEERE.getAsEmoji())
        );
    }
}
