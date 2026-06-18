package ch.frily.yubot.scheduler;

import ch.frily.yubot.feature.ActiveMod;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.feature.ClosureRepository;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ActiveModActivityScheduler implements Scheduler {

    @Override
    public void execute() throws SQLException {
        List<ActiveMod> outdatedActiveMods = ClosureRepository.getModerators().stream().filter(activeMod -> {
                    return activeMod.lastActivityAt().isBefore(LocalDateTime.now().minusMinutes(Closure.getMIN_INACTIVITY_TIME()));
        }).toList();

        outdatedActiveMods.forEach(Closure::requestActivityProve);
        log.debug("Executed ActiveModActivityScheduler: {} outdated active mods found out of {} active mods.", outdatedActiveMods.size(), ClosureRepository.getModerators().size());
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
