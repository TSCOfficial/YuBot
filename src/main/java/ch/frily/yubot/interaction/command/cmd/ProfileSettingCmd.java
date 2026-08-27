package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.Profile;
import ch.frily.yubot.feature.ProfileRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reflections.Reflections.log;

/**
 * Command to control the profile settings.
 * <p>
 *     It uses the enum of {@link ProfileRepository.Setting} to get the options, the possible arguments (as autocomplete) and such.
 * </p>
 * @author Aliz frily
 */
public class ProfileSettingCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "setting";
    }

    @Override
    public String getDescription() {
        return "Ändere deine Profileinstellungen";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        List<OptionMapping> options = Arrays.stream(ProfileRepository.Setting.values()).map(setting -> {
            return event.getOption(setting.getLabel());
        }).filter(Objects::nonNull).toList();

        StringBuilder modifiedSettingsSB = new StringBuilder();
        options.forEach(option -> {
            log.info(option.getName());
            log.info(option.getAsString());
            try {
                ProfileRepository.Setting setting = ProfileRepository.getSettingByLabel(option.getName());
                if (setting.getDataType() == Boolean.class) {
                    ProfileRepository.setSetting(event.getMember(), setting, option.getAsBoolean());
                } else {
                    ProfileRepository.setSetting(event.getMember(), setting, option.getAsString());
                }
                modifiedSettingsSB.append(String.format("**%s** geändert auf **%s**\n", setting.getLabel(), option.getAsString()));
            } catch (Exception e) {
                ExceptionHandler.handle(e, event);
            }
        });

        if (modifiedSettingsSB.length() > 0) {
            event.reply("Einstellungen erfolgreich gespeichert:\n" + modifiedSettingsSB.toString()).setEphemeral(true).queue();
        } else {
            event.reply("Es wurden keine Einstellungen geändert.\n-# Du hast keine Optionen angewählt oder die angegebenen Werte sind ungültig.").setEphemeral(true).queue();
        }
    }

    @Override
    public List<OptionData> getOptions() {
        return Arrays.stream(ProfileRepository.Setting.values()).map(setting -> new OptionData( // todo filter out the ones that normal user shouln't see!
                setting.getDataType() == Boolean.class ? OptionType.BOOLEAN : OptionType.STRING,
                setting.getLabel(),
                "keine Beschreibung vorhanden",
                false,
                setting.getDataType() == Boolean.class ? false : true
        )).toList();
    }

    @Override
    public Map<String, List<?>> getAutocomplete() {
        return Arrays.stream(ProfileRepository.Setting.values())
                .filter(setting -> setting.getAutocompleteOptions() != null)
                .collect(Collectors.toMap(setting -> setting.getLabel(), setting -> setting.getAutocompleteOptions()));
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_ACTIVEMOD
        ).map(EnvResolver::getRoleById).toList();
    }
}
