package ch.frily.yubot.scheduler;


import java.sql.SQLException;

/**
 * Run a task in an interval of x units
 */
public interface IScheduler {
    /**
     * Define when {@link #execute()} should be executed
     * <p>
     *     Minute Hour Day Month Weekday<br>
     *     Example: 0 * * * * -> Each full hour, at minute 0
     * </p>
     * @return
     */
    String cronExpression();

    void execute() throws SQLException, ClassNotFoundException;
}
