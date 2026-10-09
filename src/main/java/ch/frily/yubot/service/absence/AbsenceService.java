package ch.frily.yubot.service.absence;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.interaction.select.ISelect;
import ch.frily.yubot.scheduler.IScheduler;
import ch.frily.yubot.service.Service;
import ch.frily.yubot.service.absence.button.*;
import ch.frily.yubot.service.absence.modal.AbsenceAddModal;
import ch.frily.yubot.service.absence.schedule.AbsenceContainerScheduler;

import java.util.List;

public class AbsenceService extends Service {
    @Override
    public String getName() {
        return "Abwesenheitsmanagement";
    }

    @Override
    public String getDescription() {
        return "Verwalte deine Abwesenheiten und schau wer wann abwesend ist.";
    }

    @Override
    public List<Button> getButtons() {
        return List.of(
                new AbsenceAddBtn(),
                new AbsenceApproveDeleteBtn(),
                new AbsenceCancelDeleteBtn(),
                new AbsenceDetailBtn(),
                new AbsenceEditBtn(),
                new AbsenceEditOwnBtn()
        );
    }

    @Override
    public List<Modal> getModals() {
        return List.of(
                new AbsenceAddModal()
        );
    }

    @Override
    public List<IScheduler> getSchedulers() {
        return List.of(
                new AbsenceContainerScheduler()
        );
    }
}
