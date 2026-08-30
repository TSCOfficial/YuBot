package ch.frily.yubot.feature;

import net.dv8tion.jda.api.entities.Member;

public record Profile(Member member, Boolean activeModSendInDm, String absenceNotice) {
}
