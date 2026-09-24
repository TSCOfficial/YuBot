package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
public class SayCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "say";
    }

    @Override
    public String getDescription() {
        return "Sag etwas über den YuBot";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "text", "Der Text den du senden möchtest.", true),
                new OptionData(OptionType.INTEGER, "delete-after", "Löscht die Nachricht nach angegebener Zeit (personalisierte Zeitangabe: In Sekunden)", false, true)
        );
    }

    @Override
    public Map<String, List<Command.Choice>> getAutocomplete(CommandAutoCompleteInteractionEvent event) {
        return Map.of(
                "delete-after", List.of(
                        new Command.Choice("10s", 10),
                        new Command.Choice("30s", 30),
                        new Command.Choice("1min", 60),
                        new Command.Choice("2min", 120),
                        new Command.Choice("5min", 300),
                        new Command.Choice("10min", 600),
                        new Command.Choice("15min", 900),
                        new Command.Choice("130min", 1800),
                        new Command.Choice("1h", 3800),
                        new Command.Choice("2h", 7200)
                )
        );
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_SERVERLEITUNG
        ).map(EnvResolver::getRoleById).toList();
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        String text = event.getOption("text").getAsString();
        Integer deleteAfter = event.getOption("delete-after") != null ? event.getOption("delete-after").getAsInt() : null;
        log.info(String.valueOf(deleteAfter));
        if (deleteAfter != null) {
            long timestamp = Instant.now().plusSeconds(deleteAfter).toEpochMilli() / 1000;
            text += String.format("""
                    
                    -# *<:timer:1522290651742339122> Nachricht wird <t:%d:R> gelöscht.*
                    """, timestamp
            );
        }

        event.getChannel().sendMessage(text).queue(message -> {
            String deleteAfterInfo = "";
            if (deleteAfter != null) {
                log.info("setting delete after to {}", deleteAfter);
                message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS);
                deleteAfterInfo = String.format(" und wird in %s gelöscht.", Util.calcDurationSeconds(deleteAfter));
            }
            event.reply(String.format("✅ Die Nachricht wurde erfolgreich gesendet%s", deleteAfterInfo.isBlank() ? "." : deleteAfterInfo)).setEphemeral(true).queue();
        });
    }
}
