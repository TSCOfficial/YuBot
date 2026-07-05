package ch.frily.yubot.scheduler;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ActiveModActivityScheduler implements Scheduler {

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
                Closure.requestActivityProve(activeMod);
            } catch (Exception exception) {
                ExceptionHandler.handle(exception);
            }
        });
    }

    @Override
    public long interval() {
        return 60;
    }

    @Override
    public TimeUnit timeUnit() {
        return TimeUnit.SECONDS;
    }
}
