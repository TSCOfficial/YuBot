package ch.frily.yubot.service.profile.slashcommand;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class SubcmdTest implements ISlashSubcommand {
    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getDescription() {
        return "fjsnjpifsw";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        log.info("Executing subcommand test");
    }
}
