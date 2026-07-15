package ch.frily.yubot.interaction.select.select;

import ch.frily.yubot.container.ActiveModStatisticDetailContainer;
import ch.frily.yubot.feature.ActiveModTracking;
import ch.frily.yubot.feature.ActiveModTrackingRepository;
import ch.frily.yubot.interaction.select.IStringSelect;
import ch.frily.yubot.util.Util;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ActiveModTrackingDetailSelect implements IStringSelect {

    @Getter
    @Setter
    private Map<Member, List<ActiveModTracking>> activeModTrackingMap;

    @Override
    public String getId() {
        return "active-mod-tracking-detail-select";
    }

    @Override
    public String getPlaceholder() {
        return "Moderator*in auswählen";
    }

    @Override
    public Integer getMinValues() {
        return 1;
    }

    @Override
    public Integer getMaxValues() {
        return 1;
    }

    @Override
    public List<SelectOption> getOptions() {
        return activeModTrackingMap.entrySet().stream()
                .map(entry -> {
                    SelectOption option = SelectOption.of(entry.getKey().getEffectiveName(), entry.getKey().getId());
                    ActiveModTracking thisMonthsTracking = entry.getValue().stream().filter(tracking -> Objects.equals(tracking.month(), YearMonth.now())).findFirst().orElse(null);
                    if (thisMonthsTracking != null) {
                        option = option.withDescription(String.format("Dieser Monat: %s", Util.calcDuration(thisMonthsTracking.activeTime())));
                    } else {
                        option = option.withDescription("Dieser Monat: *Nicht registriert*");
                    }
                    return option;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void execute(@NonNull StringSelectInteractionEvent event) throws SQLException, ClassNotFoundException {
        log.info(event.getInteraction().getSelectedOptions().getFirst().getValue());
        String selectedMemberId = event.getInteraction().getSelectedOptions().getFirst().getValue();
        Member selectedMember = event.getGuild().getMemberById(selectedMemberId);
        Map<Member, List<ActiveModTracking>> activeModTrackingMap = ActiveModTrackingRepository.getActiveModTrackingsAsMap();
        ActiveModStatisticDetailContainer activeModStatisticDetailContainer = new ActiveModStatisticDetailContainer(selectedMember, activeModTrackingMap);
        event.editComponents(activeModStatisticDetailContainer.build()).useComponentsV2().queue();
    }
}
