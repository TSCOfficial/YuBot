package ch.frily.yubot.feature.ticket;

import ch.frily.yubot.feature.Feature;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.button.btn.ticket.*;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.command.cmd.ticket.TicketCmdGroup;
import ch.frily.yubot.interaction.contextmenu.IContextMenu;
import ch.frily.yubot.interaction.contextmenu.ctxmenu.ModticketCtxMenu;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.interaction.modal.modal.TicketSummaryModal;
import ch.frily.yubot.interaction.modal.modal.TicketTypeSelectorModal;
import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.scheduler.schedules.TicketActivityScheduler;

import java.util.List;

public class TicketFeature extends Feature {

    private static TicketFeature instance;

    public static TicketFeature getInstance() {
        if (instance == null) {
            instance = new TicketFeature();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Ticketsystem";
    }

    @Override
    public String getDescription() {
        return "Ermöglichst ein geschützten Kommunikationskanal mit dem Serverteam. Zwei-Stufen-Ticketsystem.";
    }

    @Override
    public List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of(
                new TicketCmdGroup()
        );
    }

    @Override
    public List<Button> getButtons() {
        return List.of(
                new TicketCancelOpenBtn(),
                new TicketCloseRequestAcceptBtn(),
                new TicketCloseRequestBtn(),
                new TicketCloseRequestRejectBtn(),
                new TicketConfirmOpenBtn(),
                new TicketDeleteBtn(),
                new TicketPanelSupportBtn(),
                new TicketPanelAwarenessBtn(),
                new TicketPanelBewerbungBtn()
        );
    }

    @Override
    public List<Modal> getModals() {
        return List.of(
                new TicketSummaryModal(),
                new TicketTypeSelectorModal()
        );
    }

    @Override
    public List<IScheduler> getSchedulers() {
        return List.of(
                new TicketActivityScheduler()
        );
    }

    @Override
    public List<IContextMenu> getContextMenus() {
        return List.of(
                new ModticketCtxMenu()
        );
    }
}
