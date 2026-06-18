package ch.frily.yubot.scheduler;


import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * Run a task in an interval of x units
 */
public interface Scheduler {
    void execute() throws SQLException;

    long interval();

    TimeUnit timeUnit();
}
