package ch.frily.yubot.container;

import ch.frily.yubot.feature.ActiveMod;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.entities.Role;

import java.time.ZoneId;
import java.time.ZoneOffset;

public class ClosureActivityRequestContainer extends Container {

    public ClosureActivityRequestContainer(ActiveMod activeMod) {
        long epochTime = activeMod.lastActivityAt().atZone(EnvResolver.getZoneId()).toEpochSecond();
        Role activeModRole = EnvResolver.getRoleById(EnvKey.ROLE_ACTIVEMOD);
        int activeModCount = Closure.getActiveMods().size() - 1; // mod-count without this mod

        this.addTextDisplay(String.format("## %s, Bestätige deine Anwesenheit", activeMod.member().getAsMention()));
        this.addTextDisplay(String.format("Deine letzte Aktivität war um <t:%d:T> (<t:%d:R>). Bitte bestätige deine Anwesenheit.", epochTime, epochTime));
        this.addTextDisplay(String.format("Du kannst deine %s Rolle auch direkt entfernen falls du keine Kapazität mehr hast.", activeModRole.getAsMention()));

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        if (activeModCount > 1) {
            this.addTextDisplay(String.format("Es sind noch **%d** Moderatoren*innen aktiv (exkl. du).", activeModCount));
        } else if (activeModCount == 1) {
            this.addTextDisplay("Es ist noch **1** Moderator*in aktiv (exkl. du).");
        } else {
            this.addTextDisplay("⚠️ Es sind **keine** weiteren Moderatoren*innen aktiv ausser du.");
        }

        this.addTextDisplay(
                String.format("-# Du hast %d Minuten Zeit um deine Anwesenheit zu bestätigen. Falls du nicht bestätigst, wird dir die %s Rolle automatisch entfernt.",
                        Closure.getMAX_ACTIVITY_REQUEST_RESPONSE_MINUTES(), activeModRole
                )
        );
    }
}
