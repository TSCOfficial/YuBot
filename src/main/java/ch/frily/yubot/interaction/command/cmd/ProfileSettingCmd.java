package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.ProfileRepository;
import ch.frily.yubot.feature.Setting;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reflections.Reflections.log;

/**
 * Command to control the profile settings.
 * <p>
 *     It uses the enum of {@link Setting} to get the options, the possible arguments (as autocomplete) and such.
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
        List<OptionMapping> options = Arrays.stream(Setting.values()).map(setting -> {
            return event.getOption(setting.getLabel());
        }).filter(Objects::nonNull).toList();

        StringBuilder modifiedSettingsSB = new StringBuilder();
        StringBuilder failedSettingsSB = new StringBuilder();
        for (OptionMapping option : options) {
            log.info(option.getName());
            log.info(option.getAsString());
            try {
                Setting setting = Setting.getSettingByLabel(option.getName());
                if (!Util.isPermitted(event.getMember(), setting.getAllowedRoles())) {
                    failedSettingsSB.append(String.format("- `%s`: Du bist nicht berechtigt diese Einstellung zu ändern.\n", setting.getLabel()));
                }
                if (validateInput(option, setting)) {
                    if (setting.getDataType() == Boolean.class) {
                        // Validate specific setting
                        Optional<String> specificFailure = runSettingSpecificValidation(setting, option, event);
                        if (specificFailure.isPresent()) {
                            failedSettingsSB.append(specificFailure.get());
                            continue;
                        }
                        ProfileRepository.upsertSetting(event.getMember(), setting, option.getAsBoolean());
                    } else {
                        ProfileRepository.upsertSetting(event.getMember(), setting, option.getAsString());
                    }
                    modifiedSettingsSB.append(String.format("- `%s`: geändert auf __%s__.\n", setting.getLabel(), option.getAsString()));
                } else {
                    failedSettingsSB.append(String.format("- `%s`: __%s__ ist keine gültige Option.\n", setting.getLabel(), option.getAsString()));
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e, event);
            }
        };

        StringBuilder resultStringSB = new StringBuilder();
        if (modifiedSettingsSB.toString().isBlank()) {
            resultStringSB.append("❌ **Es wurden keine Einstellungen geändert.**\n-# Du hast keine Optionen angewählt oder die angegebenen Werte sind ungültig.");
        } else {
            log.info("Einstellungen erfolgreich gespeichert: {} ({})", modifiedSettingsSB, modifiedSettingsSB.length());
            resultStringSB.append(String.format("""
                    ✅ **Einstellungen erfolgreich gespeichert:**\n
                    %s
                    """, modifiedSettingsSB)
            );
        }
        log.info("Folgende Einstellungen konnten nicht gespeichert werden: {} ({})", failedSettingsSB, failedSettingsSB.length());
        if (!failedSettingsSB.toString().isBlank()) {
            resultStringSB.append(String.format("""
                            \n
                            
                            ⚠️ **Folgende Einstellungen konnten nicht gespeichert werden:**\n%s
                            """, failedSettingsSB)
            );
        }
        event.reply(resultStringSB.toString()).setEphemeral(true).queue();
    }

    /**
     * Checks if the input is a valid value for the given setting, to prevent false input due to the autocomplete feature
     * <p>
     *     Autocomplete options are only allowed for non-boolean settings, so the method can handle any input as a string
     * </p>
     * @param inputOption
     * @param setting
     * @return
     */
    private boolean validateInput(OptionMapping inputOption, Setting setting){
        if (setting.getDataType() == Boolean.class){
            return true;
        }
        List<String> autocompleteOptions = setting.getAutocompleteOptions();
        if(autocompleteOptions == null){
            return true;
        }
        return autocompleteOptions.contains(inputOption.getAsString());
    }

    /**
     * Zentraler Dispatcher für settingspezifische Validierungen.
     * Neue Validierung hinzufügen = neuer case, keine zusätzliche Verschachtelung.
     *
     * @return leeres Optional wenn gültig/erfolgreich, sonst die fertige Fehlermeldung für die Ausgabe
     */
    private Optional<String> runSettingSpecificValidation(Setting setting, OptionMapping option, SlashCommandInteractionEvent event) {
        return switch (setting) {
            case ACTIVEMOD_SEND_IN_DM -> validateActiveModSendIn(event)
                    ? Optional.empty()
                    : Optional.of(String.format(
                    "- `%s` konnte nicht auf __%s__ gesetzt werden.\n> -# Deine Datenschutz Einstellungen erlauben keine DMs oder du hast den Bot blockiert.\n",
                    setting.getLabel(), option.getAsBoolean()));
            default -> Optional.empty();
        };
    }

    /**
     * Prüft, ob dem User eine DM gesendet werden kann.
     * @return true wenn erfolgreich zugestellt, false wenn nicht (z. B. DMs blockiert)
     */
    private boolean validateActiveModSendIn(SlashCommandInteractionEvent event) {
        try {
            PrivateChannel privateChannel = event.getMember().getUser().openPrivateChannel().complete();
            privateChannel.sendMessage("ℹ️ Du erhälst absofort die ActiveMod-Nachrichten via DM.").complete();
            return true;
        } catch (ErrorResponseException ere) {
            if (ere.getErrorResponse() == ErrorResponse.CANNOT_SEND_TO_USER) {
                return false;
            }
            ExceptionHandler.handle(ere, event);
            return false;
        }
    }

    @Override
    public List<OptionData> getOptions() {
        return Arrays.stream(Setting.values()).map(setting -> new OptionData( // todo filter out the ones that normal user shouln't see!
                setting.getDataType() == Boolean.class ? OptionType.BOOLEAN : OptionType.STRING,
                setting.getLabel(),
                setting.getDescription(),
                false,
                setting.getAutocompleteOptions() == null ? false : true
        )).toList();
    }

    @Override
    public Map<String, List<?>> getAutocomplete(CommandAutoCompleteInteractionEvent event) {
        return Arrays.stream(Setting.values())
                .filter(setting -> setting.getAutocompleteOptions() != null)
                .filter(setting -> Util.isPermitted(event.getMember(), setting.getAllowedRoles()))
                .collect(Collectors.toMap(setting -> setting.getLabel(), setting -> setting.getAutocompleteOptions()));
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_ACTIVEMOD
        ).map(EnvResolver::getRoleById).toList();
    }
}
