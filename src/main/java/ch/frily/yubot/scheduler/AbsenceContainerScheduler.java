package ch.frily.yubot.scheduler;

import ch.frily.yubot.feature.DynamicMessageList;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class AbsenceContainerScheduler implements IScheduler{
    @Override
    public void execute() throws SQLException, ClassNotFoundException {
        DynamicMessageList.ABSENCES.update();
        log.info("Updated absences container");
    }

    @Override
    public String cronExpression() {
        return "* * * * *";
    }
}
