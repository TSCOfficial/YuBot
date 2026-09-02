package ch.frily.yubot.feature.setting;

import net.dv8tion.jda.api.entities.Member;

public record Settings(Member member, Boolean activeModSendInDm, String absenceNotice) {
}
