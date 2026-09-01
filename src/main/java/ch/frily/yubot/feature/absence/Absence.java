package ch.frily.yubot.feature.absence;

import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDateTime;

public record Absence(Integer id, Member member, LocalDateTime fromDateTime, LocalDateTime toDateTime, AbsenceType type, String reason, boolean absenceMessage, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
