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
        selectBuilder.addOptions(Setting.ACTIVEMOD_GRAPH_COLOR.getChoices().stream().map(SettingChoice::toSelectOption).toArray(SelectOption[]::new));

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
        SelectOption selectedOption = setting.getChoices().stream().filter(option -> Objects.equals(option.value(), colorValue)).findFirst().orElseThrow().toSelectOption();
        String replyMessage = String.format("✅ Die Einstellung **%s** wurde auf %s `%s` festgelegt.", setting.getLabel(), selectedOption.getEmoji(), selectedOption.getLabel());
        event.reply(replyMessage).setEphemeral(true).queue();
    }
}
