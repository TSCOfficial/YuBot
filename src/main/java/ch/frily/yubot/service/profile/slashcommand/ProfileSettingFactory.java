package ch.frily.yubot.service.profile.slashcommand;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.service.profile.Setting;
import ch.frily.yubot.service.profile.SettingContext;
import ch.frily.yubot.service.profile.SettingChoice;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProfileSettingFactory implements ISlashSubcommand {

    private final String name;
    private final String description;
    private final OptionData option;
    private final List<SettingChoice> autocompleteChoices;
    private final Setting setting;
    private final List<Role> allowedRoles;
    private final Function<SettingContext, String> onSubmit;

    public ProfileSettingFactory(Setting setting) {
        this.setting = setting;
        this.name = setting.getLabel();
        this.description = setting.getDescription();
        this.option = setting.getOption();
        this.autocompleteChoices = setting.getAutocompleteChoices();
        this.allowedRoles = setting.getAllowedRoles();
        this.onSubmit = setting.getOnSubmit();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        OptionMapping optionMapping = Objects.requireNonNull(event.getOption(option.getName()));
        String value = optionMapping.getAsString();
        SettingContext context = new SettingContext(event.getMember(), setting, value);
        onSubmit.apply(context);
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(option);
    }

    @Override
    public Map<String, List<Command.Choice>> getAutocomplete(CommandAutoCompleteInteractionEvent event) throws SQLException {
        List<Command.Choice> choices = autocompleteChoices.stream().map(choice -> {
            return new Command.Choice(choice.label(), String.valueOf(choice.value()));
        }).toList();

        return Map.of(option.getName(), choices);
    }

    @Override
    public List<Role> getAllowedRoles() {
        return this.allowedRoles;
    }
}
