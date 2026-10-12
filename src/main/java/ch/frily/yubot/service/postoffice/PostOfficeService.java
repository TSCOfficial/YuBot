package ch.frily.yubot.service.postoffice;

import ch.frily.yubot.interaction.command.ISlashCommand;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.service.Service;
import ch.frily.yubot.service.postoffice.slashcommand.SayCmd;
import ch.frily.yubot.service.postoffice.slashcommand.SendCmdGroup;

import java.util.List;

public class PostOfficeService extends Service {

    private static PostOfficeService instance;
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
        return "Versende Nachrichten mit verschiedenen Inhalten.";
    }

    @Override
    public List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of(
                new SendCmdGroup()
        );
    }

    @Override
    public List<ISlashCommand> getSlashCommands() {
        return List.of(
                new SayCmd()
        );
    }
}
