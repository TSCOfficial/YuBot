package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.container.ActiveModStatisticContainer;
import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.feature.ActiveModTrackingRepository;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class ActiveModStatisticCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "statistic";
    }

    @Override
    public String getDescription() {
        return "Erhalte einblick in die aktiven Moderatoren";
    }

    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        List<ActiveModTracking> activeModTrackings = ActiveModTrackingRepository.getActiveModTrackings();
        ActiveModStatisticContainer activeModStatisticContainer = new ActiveModStatisticContainer(activeModTrackings, event.getMember());
        event.replyComponents(activeModStatisticContainer.build()).useComponentsV2().setAllowedMentions(List.of()).setEphemeral(true).queue();
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_MODERATOR
        ).map(EnvResolver::getRoleById).toList();
    }
}
