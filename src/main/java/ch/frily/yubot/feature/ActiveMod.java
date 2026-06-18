package ch.frily.yubot.feature;

import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDateTime;

public record ActiveMod(Member member, LocalDateTime lastActivityAt) {
}
