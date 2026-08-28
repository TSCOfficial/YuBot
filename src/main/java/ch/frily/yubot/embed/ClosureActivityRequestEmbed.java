package ch.frily.yubot.embed;


import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.feature.ActiveMod;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.feature.ProfileRepository;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.time.Instant;

@Slf4j
public class ClosureActivityRequestEmbed implements IEmbed {

    private final ActiveMod activeMod;
    private final long epochTime;
    private final Role activeModRole;
    private final int activeModCount;

    public ClosureActivityRequestEmbed(ActiveMod activeMod) {
        this.activeMod = activeMod;
        this.epochTime = activeMod.lastActivityAt().atZone(EnvResolver.getZoneId()).toEpochSecond();
        this.activeModRole = EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD);
        this.activeModCount = Closure.getActiveMods().size() - 1; // mod-count without this mod
    }

    @Override
    public String getDescription() {
        boolean activeModSendInDm = false;
        try {
            activeModSendInDm = ProfileRepository.getProfileOrThrow(activeMod.member()).activeModSendInDm().equals("Via DM");
        } catch (Exception e) {
            // already false if no profile exist
        }
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("## %s, bestätige deine Anwesenheit\n", activeMod.member().getEffectiveName()));
        builder.append(String.format("Deine letzte Aktivität war um <t:%d:T> (<t:%d:R>). Bitte bestätige deine Anwesenheit.\n", epochTime, epochTime));
        builder.append("Du kannst dich auch direkt opt-outen, falls du keine Kapazität mehr hast.\n");

        builder.append("\n");

        if (activeModCount > 1) {
            builder.append(String.format("Es sind noch **%d** weitere Moderatoren*innen aktiv (exkl. du).\n", activeModCount));
        } else if (activeModCount == 1) {
            builder.append("Es ist nur noch **1** weitere*r Moderator\\*in aktiv (exkl. du).\n");
        } else {
            builder.append("⚠️ Es sind **keine** weiteren Moderatoren\\*innen aktiv ausser du. Falls du nicht bestätigst oder keine Kapazität mehr hast, wird der Server geschlossen.\n");
        }

        builder.append(
                String.format("-# Du hast %d Minuten Zeit um deine Anwesenheit zu bestätigen. Falls du nicht bestätigst, wird dir die %s-Rolle automatisch entfernt.",
                        Closure.getMAX_NORMAL_ACTIVITY_REQUEST_RESPONSE_TIME(), activeModSendInDm ? "ActiveMod" : activeModRole
                )
        );

        return builder.toString();
    }

    @Override
    public Color getColor() {
        return ch.frily.yubot.util.Color.RED;
    }

    @Override
    public Instant getTimestamp() {
        return null;
    }
}
