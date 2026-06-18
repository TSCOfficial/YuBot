package ch.frily.yubot.scheduler;

import ch.frily.yubot.exception.ExceptionHandler;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@UtilityClass
public class SchedulerRegistry {

    private static final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(4);

    public static void registerAll() {
        List<Scheduler> schedulers = List.of(
                new ActiveModActivityScheduler()
        );

        for (Scheduler scheduler : schedulers) {
            executor.scheduleAtFixedRate(
                    () -> {
                        try {
                            scheduler.execute();
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    },
                    0,
                    scheduler.interval(),
                    scheduler.timeUnit()
            );
            log.info("Registered scheduler: {}", scheduler.getClass().getSimpleName());
        }
    }
}
