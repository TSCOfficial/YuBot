package ch.frily.yubot.service.profile.slashcommand;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.service.profile.Setting;
import ch.frily.yubot.service.profile.SettingContext;
import ch.frily.yubot.service.profile.SettingChoice;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Generate a setting-command
 * <p>
 *     This takes the arguments of the setting to generate a subcommand.<br>
 *     This can also be used to execute a modal
 * </p>
 */
@Slf4j
public class ProfileSettingFactory implements ISlashSubcommand {

    private String name = "default";
    private String description = "default";
    private OptionData option;
    private List<SettingChoice> autocompleteChoices;
    private Setting setting;
    private List<Role> allowedRoles;
    private Function<SettingContext, String> onSubmit;

    /**
     * Define the setting for which the command should be build
     * @param setting
     * @return This instance, for chaining
     */
    public ISlashSubcommand setSetting(Setting setting) {
        this.setting = setting;
        this.name = setting.getLabel();
        this.description = setting.getDescription();
        this.option = setting.getOption();
        this.autocompleteChoices = setting.getAutocompleteChoices();
        this.allowedRoles = setting.getAllowedRoles();
        this.onSubmit = setting.getOnSubmit();
        return this;
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
        SettingContext context = new SettingContext(setting, event);
        onSubmit.apply(context);
    }

    @Override
    public List<OptionData> getOptions() {
        if (option != null) {
            return List.of(option);
        } else {
            return List.of();
        }

    }

    @Override
    public Map<String, List<Command.Choice>> getAutocomplete(CommandAutoCompleteInteractionEvent event) throws SQLException {
        if (!autocompleteChoices.isEmpty()) {
            List<Command.Choice> choices = autocompleteChoices.stream().map(choice -> {
                return new Command.Choice(choice.label(), String.valueOf(choice.value()));
            }).toList();

            return Map.of(option.getName(), choices);
        } else {
            return Map.of();
        }

    }

    @Override
    public List<Role> getAllowedRoles() {
        return this.allowedRoles;
    }
}
