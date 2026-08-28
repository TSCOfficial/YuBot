package ch.frily.yubot.interaction.command;

import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.interaction.command.cmd.*;
import ch.frily.yubot.util.Util;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SlashCommandRegistry {

    private static SlashCommandRegistry instance;

    // name, SlashCommand
    private final Map<String, ISlashCommand> commands = new HashMap<>();
    private final List<ISlashCommandGroup> groups = new ArrayList<>();
    // group-subname, Slashcommand
    private final Map<String, ISlashSubcommand> subcommands = new HashMap<>();

    public static SlashCommandRegistry getInstance() {
        if (instance == null) {
            instance = new SlashCommandRegistry();
        }
        return instance;
    }

    /**
     * Load the slashcommands
     */
    public void loadCommands() {
        List<ISlashCommand> slashCommands = List.of(
                new TestCmd() // tests only
        );

        List<ISlashCommandGroup> slashCommandGroups = List.of(
                new TicketCmdGroup(),
                new ActiveModCmdGroup(),
                new SendCmdGroup(),
                new MentionCmdGroup(),
                new ProfileCmdGroup()
        );

        slashCommands.forEach(cmd -> {
            commands.put(cmd.getName(), cmd);
        });

        slashCommandGroups.forEach(group -> {
            groups.add(group);
            group.getSubcommands().forEach(cmd -> {
                subcommands.put(group.getName() + " " + cmd.getName(), cmd);
            });

        });
    }

    /**
     * Prepare the commands to be able to register them
     * @return list of prepared commands
     */
    public List<CommandData> prepareCommandsForRegistry() {
        List<CommandData> commandDataList = new ArrayList<>();

        commands.forEach((_, cmd) -> {
            commandDataList.add(buildCommand(cmd));
        });

        groups.forEach(group -> {
            commandDataList.add(buildGroup(group));
        });

        return commandDataList;
    }

    private SlashCommandData buildCommand(ISlashCommand command) {
        SlashCommandData slashCommand = Commands.slash(command.getName(), command.getDescription());
        if (!command.getOptions().isEmpty()) {
            slashCommand.addOptions(command.getOptions());
        }
        if (!command.getDefaultPermissions().isEmpty()) {
            slashCommand.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.getDefaultPermissions()));
        }

        return slashCommand;
    }

    private SlashCommandData buildGroup(ISlashCommandGroup group) {
        SlashCommandData slashCommand = Commands.slash(group.getName(), "no-description-set");
        if (!group.getDefaultPermissions().isEmpty())
            slashCommand.setDefaultPermissions(DefaultMemberPermissions.enabledFor(group.getDefaultPermissions()));

        group.getSubcommands().forEach(sub -> {
            SubcommandData subData = new SubcommandData(sub.getName(), sub.getDescription());
            if (!sub.getOptions().isEmpty()) subData.addOptions(sub.getOptions());
            slashCommand.addSubcommands(subData);
        });

        return slashCommand;
    }

    /**
     * Dispatch the event from an eventlistener to the appropriate interaction executor
     * @param event
     */
    public void dispatchInteractionEvent(SlashCommandInteractionEvent event) throws NotFoundException, SQLException, ClassNotFoundException {
        Map<String, ISlashCommand> allSlashCommands = new HashMap<>();
        allSlashCommands.putAll(commands);
        allSlashCommands.putAll(subcommands);

        ISlashCommand command = allSlashCommands.get(event.getFullCommandName());
        if (command == null) {
            throw new NotFoundException(String.format("Slashcommand '%s' konnte nicht gefunden werden.", event.getFullCommandName()));
        }

        // Check if user is allowed to execute command
        if (!Util.isAdministrator(event.getMember()) && !command.getAllowedRoles().isEmpty() && command.getAllowedRoles().stream().noneMatch(role -> event.getMember().getRoles().contains(role))) {
            throw new PermissionDeniedException(String.format("Nur Mitglieder\\*innen mit einer der folgenden Rollen können diesen Befehl ausführen: %s", String.join(", ", command.getAllowedRoles().stream().map(role -> role.getAsMention()).toList())));
        }

        command.execute(event);
    }

    public void dispatchAutocompleteEvent(CommandAutoCompleteInteractionEvent event) {
        Map<String, ISlashCommand> allSlashCommands = new HashMap<>();
        allSlashCommands.putAll(commands);
        allSlashCommands.putAll(subcommands);

        ISlashCommand command = allSlashCommands.get(event.getFullCommandName());
        if (command == null) {
            return;
        }

        String focusedOptionName = event.getFocusedOption().getName();
        List<?> choices = command.getAutocomplete(event).getOrDefault(focusedOptionName, List.of());

        List<Command.Choice> options = choices.stream()
                .filter(
                        choice -> choice.toString().startsWith(event.getFocusedOption().getValue()))
                .map(choice -> {
                    if (choice instanceof String) {
                        return new Command.Choice((String) choice, (String) choice);
                    } else if (choice instanceof Integer) {
                        return new Command.Choice(choice.toString(), (Integer) choice);
                    } else if (choice instanceof Double) {
                        return new Command.Choice(choice.toString(), (Double) choice);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        event.replyChoices(options).queue();
    }
}
