package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.embed.TicketCloseRequestEmbed;
import ch.frily.yubot.embed.TicketClosedOptionsEmbed;
import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketManager;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.feature.TicketType;
import ch.frily.yubot.interaction.button.btn.TicketCloseRequestAcceptBtn;
import ch.frily.yubot.interaction.button.btn.TicketCloseRequestRejectBtn;
import ch.frily.yubot.interaction.button.btn.TicketDeleteBtn;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class TicketOpenmodCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "openmod";
    }

    @Override
    public String getDescription() {
        return "Erstelle ein Mod-Ticket";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "Für wen das Modticket geöffnet werden soll.", true)
        );
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {

        Member member = event.getOption("user").getAsMember();
        TicketManager.getInstance().createTicket(TicketType.MODTICKET, member, channel -> {
            event.reply(String.format("Modticket wurde erstellt: %s", channel.getAsMention())).setEphemeral(true).queue();
        });
    }
}
