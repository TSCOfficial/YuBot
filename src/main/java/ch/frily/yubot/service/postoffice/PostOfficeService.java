package ch.frily.yubot.service.postoffice;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.service.Service;
import ch.frily.yubot.service.postoffice.button.AddServeropenRoleBtn;
import ch.frily.yubot.service.postoffice.button.DeleteMessageBtn;
import ch.frily.yubot.service.postoffice.button.PaginationNavBtn;
import ch.frily.yubot.service.postoffice.schedule.StatusOverwriteSchedule;
import ch.frily.yubot.service.postoffice.slashcommand.SayCmd;
import ch.frily.yubot.service.postoffice.slashcommand.SendCmdGroup;
import ch.frily.yubot.service.postoffice.slashcommand.StatusCmdGroup;
import ch.frily.yubot.service.postoffice.slashcommand.SystestCmd;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The post-office is responsible for any smaller, server-wide tasks, that is used in multiple services.
 */
public class PostOfficeService extends Service {

    private static PostOfficeService instance;

    /** Status that overwrites the predefined status from env*/
    @Setter
    @Getter
    private String ephemeralStatus;

    public static PostOfficeService getInstance() {
        if (instance == null) {
            instance = new PostOfficeService();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Post office";
    }

    @Override
    public String getDescription() {
        return "Das Postamt kümmert sich um das versenden und löschen von Informationen und Nachrichten. Es übernimmt kleine Aufgaben Serverweit.";
    }

    @Override
    public List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of(
                new SendCmdGroup(),
                new StatusCmdGroup()
        );
    }

    @Override
    public List<ISlashCommand> getSlashCommands() {
        return List.of(
                new SayCmd(),
                new SystestCmd()
        );
    }

    @Override
    public List<Button> getButtons() {
        return List.of(
                new AddServeropenRoleBtn(),
                new DeleteMessageBtn(),
                new PaginationNavBtn()
        );
    }


    @Override
    public List<IScheduler> getSchedulers() {
        return List.of(
                StatusOverwriteSchedule.getInstance()
        );
    }
}
