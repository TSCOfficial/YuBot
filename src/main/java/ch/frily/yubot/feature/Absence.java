package ch.frily.yubot.feature;

import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDateTime;

public record Absence(int id, Member member, LocalDateTime fromDateTime, LocalDateTime toDateTime, String reason, String absenceMessage) {
}
