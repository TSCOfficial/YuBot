package ch.frily.yubot.service.postoffice.slashcommand;

import ch.frily.yubot.database.Database;
import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.interaction.command.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.reflections.Reflections.log;

public class SystestCmd implements ISlashCommand {
    @Override
    public String getName() {
        return "systest";
    }

    @Override
    public String getDescription() {
        return "Systemtesting - Ephemeral execution state";
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return List.of(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        event.reply("no content").queue();
    }
}
