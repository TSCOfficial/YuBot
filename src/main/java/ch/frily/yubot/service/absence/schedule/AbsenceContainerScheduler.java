package ch.frily.yubot.service.absence.schedule;

import ch.frily.yubot.service.dynamicmsg.DynamicMessageList;
import ch.frily.yubot.scheduler.IScheduler;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class AbsenceContainerScheduler implements IScheduler {
    @Override
    public void execute() throws SQLException {
        DynamicMessageList.ABSENCES.update();
    }

    @Override
    public String cronExpression() {
        return "* * * * *";
    }
}
