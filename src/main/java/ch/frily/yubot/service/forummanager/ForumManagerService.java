package ch.frily.yubot.service.forummanager;

import ch.frily.yubot.database.IRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.Service;
import ch.frily.yubot.service.forummanager.modal.CreateCustomPostModal;
import ch.frily.yubot.service.forummanager.modal.RegisterNewInterestModal;
import ch.frily.yubot.service.forummanager.slashcommand.ForumCmdGroup;

import java.util.List;

/**
 * <h1>Service</h1>
 * <p>Manages forum posts</p>
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class ForumManagerService extends Service {

    private static ForumManagerService instance;

    public static ForumManagerService getInstance() {
        if (instance == null) {
            instance = new ForumManagerService();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Forum Manager";
    }

    @Override
    public String getDescription() {
        return "Verwaltet Foren";
    }

    @Override
    public List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of(
                new ForumCmdGroup()
        );
    }

    @Override
    public List<Button> getButtons() {
        return super.getButtons();
    }

    @Override
    public List<Modal> getModals() {
        return List.of(
                new CreateCustomPostModal(),
                new RegisterNewInterestModal()
        );
    }

    @Override
    public List<IRepository> getRepositories() {
        return super.getRepositories();
    }
}
