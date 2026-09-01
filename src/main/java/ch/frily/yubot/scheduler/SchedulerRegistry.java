package ch.frily.yubot.scheduler;

import ch.frily.yubot.exception.ExceptionHandler;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@UtilityClass
public class SchedulerRegistry {

    private static final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(4);

    private static final CronParser PARSER =
            new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));

    public static void registerAll() {
        List<IScheduler> schedulers = List.of(
                new ActiveModActivityScheduler(),
                new CleanStorageScheduler(),
                new AbsenceContainerScheduler(),
                new TicketActivityScheduler(),
                new EventReminderScheduler()
        );

        for (IScheduler scheduler : schedulers) {
            scheduleNext(scheduler);
            log.info("Registered scheduler: {} ({})",
                    scheduler.getClass().getSimpleName(), scheduler.cronExpression());
        }
    }

    private static void scheduleNext(IScheduler scheduler) {
        Cron cron = PARSER.parse(scheduler.cronExpression());
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        ZonedDateTime now = ZonedDateTime.now();
        Optional<ZonedDateTime> next = executionTime.nextExecution(now);

        if (next.isEmpty()) {
            log.warn("Keine nächste Ausführungszeit für {} gefunden",
                    scheduler.getClass().getSimpleName());
            return;
        }

        long delayMs = Duration.between(now, next.get()).toMillis();

        executor.schedule(() -> {
            try {
                scheduler.execute();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                scheduleNext(scheduler);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }
}