package ch.frily.yubot.interaction.command;

import ch.frily.yubot.interaction.command.cmd.SendEmbedCmd;
import ch.frily.yubot.interaction.command.cmd.TeamlistCmd;
import ch.frily.yubot.interaction.command.cmd.ClosureCmdGroup;
import ch.frily.yubot.interaction.command.cmd.TicketCmdGroup;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

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

    public void loadCommands() {
        List<ISlashCommand> slashCommands = List.of(
                new TeamlistCmd(),
                new SendEmbedCmd()
        );

        List<ISlashCommandGroup> slashCommandGroups = List.of(
            new TicketCmdGroup(),
                new ClosureCmdGroup()
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

    public void registerAll() {
        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
        List<CommandData> commandDataList = new ArrayList<>();

        commands.forEach((name, cmd) -> {
            commandDataList.add(buildCommand(cmd));
        });

        groups.forEach(group -> {
            commandDataList.add(buildGroup(group));
        });

        guild.updateCommands().addCommands(commandDataList).queue(
                s -> log.info("Alle Commands registriert"),
                e -> log.error("Fehler beim Registrieren: ", e)
        );
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
            log.debug(sub.getName());
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
    public void dispatchInteractionEvent(SlashCommandInteractionEvent event) throws NotFoundException {
        commands.putAll(subcommands);

        ISlashCommand command = commands.get(event.getFullCommandName());

        if (command == null) {
            throw new NotFoundException("Slashcommand " + event.getFullCommandName() + " could not be found.");
        }

        command.execute(event);
    }

    public void dispatchAutocompleteEvent(CommandAutoCompleteInteractionEvent event) {
        ISlashCommand command = commands.get(event.getFullCommandName());
        String focusedOptionName = event.getFocusedOption().getName();
        List<?> choices = command.getAutocomplete().getOrDefault(focusedOptionName, List.of());

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
