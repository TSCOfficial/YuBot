package ch.frily.yubot.service.profile;

import net.dv8tion.jda.api.entities.Member;

import java.awt.*;

public record Settings(Member member, Boolean activeModSendInDm, String absenceNotice, Color activemodColor) {
}
