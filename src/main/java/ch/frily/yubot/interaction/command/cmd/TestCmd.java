package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.interaction.command.ISlashCommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

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
        throw new InvalidStateException("Du musst als <:status_online:1543868572609159239> online, <:status_idle:1543868571443265557> idle oder <:status_dnd:1543868569513623683> nicht stören markiert sein, um deine Aktivität zu bestätigen.", "Wenn du <:statusoffline:1543871842186567750> offline bist, sehen dich die Leute nicht.");
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return List.of(Permission.ADMINISTRATOR);
    }
}
