package ch.frily.yubot.service.postoffice.slashcommand;

import ch.frily.yubot.database.Table;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.service.forummanager.entity.SpecialInterestEntity;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

@Slf4j
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
        //event.reply("no content").queue();

        SpecialInterestEntity interestEntity = new SpecialInterestEntity();
        interestEntity.mapColumnToField(Table.SpecialInterestColumn.NAME).accept("System test #1");

        log.info(interestEntity.getName());
    }
}
