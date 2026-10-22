package ch.frily.yubot.service.profile;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

public record SettingContext(
        Member member,
        Setting setting,
        Object value
) {
}
