package ch.frily.yubot.service.storage.schedule;

import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.service.storage.SessionStorageService;

public class CleanStorageScheduler implements IScheduler {
    @Override
    public void execute() {
        SessionStorageService.getInstance().clean();
    }

    @Override
    public String cronExpression() {
        return "* * * * *";
    }
}
