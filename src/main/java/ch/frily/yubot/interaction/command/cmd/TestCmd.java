package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.DynamicMessageList;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.storage.SessionStorage;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TestCmd implements ISlashCommand {
    @Override
    public String getName() {
        return "testing";
    }

    @Override
    public String getDescription() {
        return "Für das testen von Funktionen oder Teilfunktionen.";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        String localStorage = SessionStorage.getInstance().getSessionStorage().stream().map(storage -> {
            return String.format("%s: %s [ttl: %s]", storage.key(), storage.storage().getValue(), storage.ttl());
        }).collect(Collectors.joining("\n"));
        if (localStorage.length() == 0) {
            localStorage = "Keine Daten vorhanden.";
        }
        event.reply(localStorage).setEphemeral(true).queue();
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return List.of(Permission.ADMINISTRATOR);
    }
}
