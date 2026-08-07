package ch.frily.yubot.scheduler;

import ch.frily.yubot.feature.DynamicMessageList;

import java.sql.SQLException;

public class AbsenceContainerScheduler implements IScheduler{
    @Override
    public void execute() throws SQLException, ClassNotFoundException {
        DynamicMessageList.ABSENCES.update();
    }

    @Override
    public String cronExpression() {
        return "* * * * *";
    }
}
