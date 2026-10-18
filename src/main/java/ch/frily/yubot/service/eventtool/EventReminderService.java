package ch.frily.yubot.service.eventtool;

import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.service.eventtool.schedule.EventReminderScheduler;
import ch.frily.yubot.service.Service;
import ch.frily.yubot.service.eventtool.slashcommand.StageModCmdGroup;

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
        return "Event-Tools";
    }

    @Override
    public String getDescription() {
        return "Verschiedene event-tools, sowie Reminder und Rollenvergabe.";
    }

    @Override
    public List<IScheduler> getSchedulers() {
        return List.of(
                new EventReminderScheduler()
        );
    }

    @Override
    public List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of(
                new StageModCmdGroup()
        );
    }
}
