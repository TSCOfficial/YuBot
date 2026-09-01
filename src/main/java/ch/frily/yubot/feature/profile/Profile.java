package ch.frily.yubot.feature.profile;

import net.dv8tion.jda.api.entities.Member;

public record Profile(Member member, Boolean activeModSendInDm, String absenceNotice) {
}
