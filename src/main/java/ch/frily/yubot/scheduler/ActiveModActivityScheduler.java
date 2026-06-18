package ch.frily.yubot.scheduler;

import ch.frily.yubot.feature.ActiveMod;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.feature.ClosureRepository;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActiveModActivityScheduler implements Scheduler {

    @Override
    public void execute() throws SQLException {
        List<ActiveMod> outdatedActiveMods = ClosureRepository.getModerators().stream().filter(activeMod -> {
                    return activeMod.lastActivityAt().isBefore(LocalDateTime.now().minusMinutes(Closure.getMAX_ACTIVITY_REQUEST_RESPONSE_MINUTES()));
        }).toList();

        outdatedActiveMods.forEach(Closure::requestActivityProve);

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
