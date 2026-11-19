package ch.frily.yubot.service.postoffice.slashcommand;

import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

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

        List<SpecialInterest> interests = SpecialInterestRepository.getInterests();

        interests.forEach(interest -> {
            log.info(String.valueOf(interest.getId()));
            log.info(interest.getName());
            log.info(interest.getDescription());
            if (Objects.nonNull(interest.getOriginalPoster())) {
                log.info(interest.getOriginalPoster().getAsMention());
            } else {
                log.info("No original poster");
            }

        });
    }
}
