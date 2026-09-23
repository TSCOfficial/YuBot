package ch.frily.yubot.feature.profile;

import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDateTime;

public record ProfileHistory (
        int id,
        String previousName,
        String newName,
        Profile profileReference,
        Member parentAccount,
        LocalDateTime createdAt
){}
