package ch.frily.yubot.container;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.ActiveModStatisticChart;
import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.feature.ActiveModTrackingRepository;
import ch.frily.yubot.interaction.button.btn.ActiveModApproveOptOutBtn;
import ch.frily.yubot.interaction.button.btn.ActiveModCancelOptOutBtn;
import ch.frily.yubot.interaction.button.btn.ActiveModOptInBtn;
import ch.frily.yubot.interaction.button.btn.ActiveModOptOutBtn;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.entities.Member;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ActiveModControlContainer extends Container {

    public ActiveModControlContainer(ContainerContext context) {
        try {
            Map<Member, List<ActiveModTracking>> activeModTrackingMap = ActiveModTrackingRepository.getActiveModTrackingsAsMap();
            activeModTrackingMap = ActiveModTrackingRepository.completeWithMissingModerators(activeModTrackingMap);
            this.addComponent(MediaGallery.of(
                    MediaGalleryItem.fromFile(new ActiveModStatisticChart().generateChart(activeModTrackingMap))
            ));


            addLineSeparator(Separator.Spacing.LARGE);
            this.addComponent(ActionRow.of(
                    new ActiveModOptInBtn().build(),
                    new ActiveModOptOutBtn().build())
            );
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

    }
}
