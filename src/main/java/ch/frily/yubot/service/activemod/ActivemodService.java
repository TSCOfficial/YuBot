package ch.frily.yubot.service.activemod;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.command.ISlashCommandGroup;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.interaction.select.ISelect;
import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.service.Service;
import ch.frily.yubot.service.activemod.button.*;
import ch.frily.yubot.service.activemod.modal.SelectActiveModSendTypeModal;
import ch.frily.yubot.service.activemod.schedule.ActiveModActivityScheduler;
import ch.frily.yubot.service.activemod.select.ActiveModTrackingDetailSelect;
import ch.frily.yubot.service.activemod.slashcommand.ActiveModCmdGroup;

import java.util.List;

public class ActivemodService extends Service {
    @Override
    public String getName() {
        return "Serverschliessung";
    }

    @Override
    public String getDescription() {
        return "Verwaltet die Schliessung des Servers anhand der Active-Mods.";
    }

    @Override
    public List<ISlashCommandGroup> getSlashCommandGroups() {
        return List.of(
                new ActiveModCmdGroup()
        );
    }

    @Override
    public List<Button> getButtons() {
        return List.of(
                new ActiveModActivityProveBtn(),
                new ActiveModActivityRejectBtn(),
                new ActiveModApproveOptOutBtn(),
                new ActiveModCancelOptOutBtn(),
                new ActiveModOptInBtn(),
                new ActiveModOptOutBtn(),
                new ActiveModShowStatisticBtn(),
                new ActiveModStatisticGoToHomeBtn()
        );
    }

    @Override
    public List<Modal> getModals() {
        return List.of(
                new SelectActiveModSendTypeModal()
        );
    }

    @Override
    public List<ISelect> getSelects() {
        return List.of(
                new ActiveModTrackingDetailSelect()
        );
    }

    @Override
    public List<IScheduler> getSchedulers() {
        return List.of(
                new ActiveModActivityScheduler()
        );
    }
}
