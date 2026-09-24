package ch.frily.yubot.scheduler;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.activemod.ActiveMod;
import ch.frily.yubot.database.repository.ActiveModRepository;
import ch.frily.yubot.database.repository.ActiveModTrackingRepository;
import ch.frily.yubot.feature.activemod.Closure;
import ch.frily.yubot.database.repository.SettingRepository;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class ActiveModActivityScheduler implements IScheduler {

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
                boolean sendInDM = false;
                if (SettingRepository.getSettings(activeMod.member()) != null) {
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
