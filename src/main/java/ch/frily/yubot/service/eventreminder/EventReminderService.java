package ch.frily.yubot.service.eventreminder;

import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.service.eventreminder.schedule.EventReminderScheduler;
import ch.frily.yubot.service.Service;

import java.util.List;

public class EventReminderService extends Service {

    private static EventReminderService instance;
    public static EventReminderService getInstance() {
        if (instance == null) {
            instance = new EventReminderService();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Event Reminder";
    }

    @Override
    public String getDescription() {
        return "Sendet eine Erinnerung 2 Stunden bevor ein Event beginnt.";
    }

    @Override
    public List<IScheduler> getSchedulers() {
        return List.of(
                new EventReminderScheduler()
        );
    }
}
