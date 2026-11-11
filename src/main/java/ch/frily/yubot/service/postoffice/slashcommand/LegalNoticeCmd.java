package ch.frily.yubot.service.postoffice.slashcommand;

import ch.frily.yubot.container.guildinfo.LegalNoticeContainer;
import ch.frily.yubot.interaction.command.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public class LegalNoticeCmd implements ISlashCommand {
    @Override
    public String getName() {
        return "legal";
    }

    @Override
    public String getDescription() {
        return "Datenschutz- und Rechtsrelevante Angaben";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        LegalNoticeContainer container = new LegalNoticeContainer();
        event.replyComponents(container.build()).useComponentsV2().queue();
    }
}
