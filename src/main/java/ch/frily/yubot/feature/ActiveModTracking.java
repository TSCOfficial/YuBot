package ch.frily.yubot.feature;

import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public record ActiveModTracking(
        Member moderator,
        int activeTime,
        LocalDateTime lastTimeActive,
        YearMonth month,
        int missedActivityRequestCount,
        int totalActivityRequestCount
        ) {
}
