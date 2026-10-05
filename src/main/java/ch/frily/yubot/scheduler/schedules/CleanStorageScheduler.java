package ch.frily.yubot.scheduler.schedules;

import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.storage.SessionStorage;

public class CleanStorageScheduler implements IScheduler {
    @Override
    public void execute() {
        SessionStorage.getInstance().clean();
    }

    @Override
    public String cronExpression() {
        return "* * * * *";
    }
}
