package ch.frily.yubot.feature;

import net.dv8tion.jda.api.entities.Member;

import java.lang.annotation.Native;
import java.time.LocalDateTime;

public record Absence(Integer id, Member member, LocalDateTime fromDateTime, LocalDateTime toDateTime, String reason, boolean absenceMessage) {
}
