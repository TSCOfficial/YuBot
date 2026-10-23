package ch.frily.yubot.service.profile;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public record SettingContext<T>(
        Setting setting,
        SlashCommandInteractionEvent event,
        T value
) {
}
