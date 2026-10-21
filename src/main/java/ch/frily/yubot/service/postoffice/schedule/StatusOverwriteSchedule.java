package ch.frily.yubot.service.postoffice.schedule;

import ch.frily.yubot.Client;
import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.service.postoffice.PostOfficeService;
import net.dv8tion.jda.api.entities.Activity;

import java.sql.SQLException;

public class StatusOverwriteSchedule implements IScheduler {

    private static StatusOverwriteSchedule instance;

    public static StatusOverwriteSchedule getInstance() {
        if (instance == null) {
            instance = new StatusOverwriteSchedule();
        }
        return instance;
    }

    @Override
    public String cronExpression() {
        return "* * * * *";
    }

    @Override
    public void execute() throws SQLException {
        String ephemeralStatus = PostOfficeService.getInstance().getEphemeralStatus();
        if (ephemeralStatus != null) {
            Client.getInstance().getClient().getPresence().setActivity(Activity.customStatus(ephemeralStatus));
        }
    }
}
