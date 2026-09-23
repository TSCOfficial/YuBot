package ch.frily.yubot.feature.profile;

import net.dv8tion.jda.api.entities.Member;

public record Profile(String profileId, Member parentAccount, String name, boolean isCurrentlyUsed, String profilePicture, String proxy, int useCount) {
}
