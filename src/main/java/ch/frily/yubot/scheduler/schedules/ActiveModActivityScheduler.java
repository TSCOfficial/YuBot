package ch.frily.yubot.scheduler.schedules;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.activemod.ActiveMod;
import ch.frily.yubot.database.repository.ActiveModRepository;
import ch.frily.yubot.database.repository.ActiveModTrackingRepository;
import ch.frily.yubot.service.activemod.Closure;
import ch.frily.yubot.database.repository.SettingRepository;
import ch.frily.yubot.scheduler.IScheduler;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ActiveModActivityScheduler implements IScheduler {

    @Override
    public void execute() throws SQLException {
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
                boolean sendInDM = false;
                if (Objects.nonNull(SettingRepository.getSettings(activeMod.member()))) {
                    sendInDM = SettingRepository.getSettings(activeMod.member()).activeModSendInDm();
                }
                if (sendInDM) {
                    Closure.requestActivityProveViaDM(activeMod);
                } else {
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
