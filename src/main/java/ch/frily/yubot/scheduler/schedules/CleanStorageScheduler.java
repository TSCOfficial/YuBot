package ch.frily.yubot.scheduler.schedules;

import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.storage.SessionStorage;

import java.sql.SQLException;

public class CleanStorageScheduler implements IScheduler {
    @Override
    public void execute() throws SQLException, ClassNotFoundException {
        SessionStorage.getInstance().clean();
    }

    @Override
    public String cronExpression() {
        return "* * * * *";
    }
}
