package ch.frily.yubot.scheduler;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.*;
import ch.frily.yubot.storage.SessionStorage;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class ActiveModActivityIScheduler implements IScheduler {

    @Override
    public void execute() throws SQLException, ClassNotFoundException {
        List<ActiveMod> outdatedActiveMods = ActiveModRepository.getModerators().stream().filter(activeMod -> {
            try {
                ActiveModTrackingRepository.upsertActiveMod(activeMod.member());
            } catch (Exception exception) {
                ExceptionHandler.handle(exception);
            }
            return activeMod.lastActivityAt().isBefore(LocalDateTime.now().minusMinutes(Closure.getMIN_INACTIVITY_TIME()));
        }).toList();

        outdatedActiveMods.forEach(activeMod -> {
            try {
                Member aliz = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getMemberById(618876411905835018L);
                log.info("******************************* ActiveMod: {}", activeMod);
                if (activeMod.member().getId().equals(aliz.getId())) {
                    log.info("******************************* Dev-Method call!");
                    Closure.requestActivityProveV2(activeMod);
                } else {
                    log.info("******************************* Normal-Method call!");
                    Closure.requestActivityProve(activeMod);
                }

            } catch (Exception exception) {
                ExceptionHandler.handle(exception);
            }
        });
    }

    @Override
    public String cronExpression() {
        return "* * * * *";
    }
}
