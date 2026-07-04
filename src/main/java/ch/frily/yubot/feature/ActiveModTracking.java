package ch.frily.yubot.feature;

import net.dv8tion.jda.api.entities.Member;

public record ActiveModTracking(Member moderator, int activeTime) {
}
