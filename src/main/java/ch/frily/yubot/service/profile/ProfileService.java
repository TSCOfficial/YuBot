package ch.frily.yubot.service.profile;

import ch.frily.yubot.event.IEvent;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.profile.button.AddProfileBtn;
import ch.frily.yubot.service.profile.button.DeleteProfileBtn;
import ch.frily.yubot.service.profile.button.EditProfileBtn;
import ch.frily.yubot.service.profile.button.UseProfileBtn;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.service.profile.ctxmenu.DeleteWebhookMsgCtxMenu;
import ch.frily.yubot.service.profile.ctxmenu.EditWebhookMsgCtxMenu;
import ch.frily.yubot.service.profile.ctxmenu.LookupProfileCtxMenu;
import ch.frily.yubot.service.profile.event.MessageReceivedEvent;
import ch.frily.yubot.service.profile.modal.AddProfileModal;
import ch.frily.yubot.service.profile.modal.DeleteProfileModal;
import ch.frily.yubot.service.profile.modal.EditWebhookMessageModal;
import ch.frily.yubot.service.profile.select.ProfileUseSelect;
import ch.frily.yubot.service.profile.slashcommand.ProfileCmdGroup;
import ch.frily.yubot.interaction.contextmenu.IContextMenu;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.interaction.select.ISelect;
import ch.frily.yubot.service.Service;
import net.dv8tion.jda.api.events.Event;

import java.util.List;

public class ProfileService extends Service {

    private static ProfileService instance;

    public static ProfileService getInstance() {
        if (instance == null) {
            instance = new ProfileService();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Profil";
    }

    @Override
    public String getDescription() {
        return "Personalisiere die Auswirkungen des YuBots auf dein Konto und verwalte deine Profile.";
    }

    @Override
    public List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of(
                new ProfileCmdGroup()
        );
    }

    @Override
    public List<Button> getButtons() {
        return List.of(
                new AddProfileBtn(),
                new EditProfileBtn(),
                new UseProfileBtn(),
                new DeleteProfileBtn()
        );
    }

    @Override
    public List<Modal> getModals() {
        return List.of(
                new AddProfileModal(),
                new EditWebhookMessageModal(),
                new DeleteProfileModal()
        );
    }

    @Override
    public List<ISelect> getSelects() {
        return List.of(
                new ProfileUseSelect()
        );
    }

    @Override
    public List<IContextMenu> getContextMenus() {
        return List.of(
                new DeleteWebhookMsgCtxMenu(),
                new EditWebhookMsgCtxMenu(),
                new LookupProfileCtxMenu()
        );
    }

    @Override
    public List<IEvent<? extends Event>> getEvents() {
        return List.of(
                MessageReceivedEvent.getInstance()
        );
    }
}
