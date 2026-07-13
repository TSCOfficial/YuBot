package ch.frily.yubot.feature;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public enum TicketType {
    MODTICKET(
            "modticket",
            "🔰 Modticket",
            null,
            """
                    Das Serverteam hat ein Ticket für dich geöffnet, um ein geschützten Chatkanal mit dir führen zu können.
                    """,
            null,
            List.of(EnvKey.ROLE_SUPPORT, EnvKey.ROLE_MODERATOR)
    ),
    // SUPPORT
    SUPPORT_GENERAL(
            "help",
            "❔ Allgemeine Frage",
            "Für allgemeine Fragen oder Support",
            """
                    Bitte beschreibe dein Anliegen so genau wie möglich:
 
                    - Worum geht es? (Kurze Zusammenfassung)
 
                    Das Supportteam meldet sich so schnell wie möglich bei dir.
                    """,
            TicketTypeGroup.SUPPORT,
            List.of(EnvKey.ROLE_SUPPORT, EnvKey.ROLE_MODERATOR)
    ),
    SUPPORT_REGELVERSTOSS(
            "report",
            "🚨 Regelverstoß melden",
            "Melde jemensch wegen eines Regelverstoßes.",
            """
                    Bitte beschreibe dein Anliegen so genau wie möglich:
 
                    1. **Grund der Meldung** – Was hat die Person getan?
                    2. **Wann ist es passiert?** – Ungefähre Uhrzeit / Datum
 
                    Das Supportteam meldet sich so schnell wie möglich bei dir.
                    """,
            TicketTypeGroup.SUPPORT,
            List.of(EnvKey.ROLE_SUPPORT, EnvKey.ROLE_MODERATOR)
    ),
    SUPPORT_PROBEME(
            "sup",
            "⚠️ Probleme",
            "Generelle Probleme melden.",
            """
                    Bitte beschreibe dein Anliegen so genau wie möglich:
                    - Worum geht es? (Kurze Zusammenfassung)
                    - Was hast du bereits versucht, um das Problem zu lösen?
                    - Seit wann besteht das Problem?
                    
                    Das Supportteam meldet sich so schnell wie möglich bei dir.
                    """,
            TicketTypeGroup.SUPPORT,
            List.of(EnvKey.ROLE_SUPPORT, EnvKey.ROLE_MODERATOR)
    ),
    SUPPORT_LEITUNG(
            "leit",
            "⚖️ Leitung",
            "Serverweite/wichtige Anliegen, Teambeschwerden o.Ä.",
            """
                    Bitte beschreibe dein Anliegen so genau wie möglich:
                    - Worum geht es? (Kurze Zusammenfassung)
                    
                    Die Serverleitung meldet sich so schnell wie möglich bei dir.
                    """,
            TicketTypeGroup.SUPPORT,
            List.of(EnvKey.ROLE_SERVERLEITUNG)
    ),
    // AWARENESS
    AWARENESS_PROBLEME(
            "awrn",
            "😞 Probleme",
            "Unwohlsein oder weitere Persönliche Schwierigkeiten",
            """
                    Beschreibe uns dein Anliegen so gut wie möglich, damit das Awareness Team dir helfen kann.
                    
                    Du must nichts Sagen was du nicht Teilen möchtest.
                    """,
            TicketTypeGroup.AWARENESS,
            List.of(EnvKey.ROLE_AWARENESS)
    ),
    // BEWERBUNG
    BEWERBUNG_SUPPORT(
            "besup",
            "🔩 Fachbereich Support",
            "Bewerbung Fachbereich Support",
            """
                    Bitte verfasse deine Bewerbung. Für deine Bewerbung ist uns Folgendes wichtig::
                    
                    - Kurze Vorstellung (Name, Alter, Pronomen, ...)
                    - Warum möchtest du Teil des Supportteams werden?
                    - Deine zeitliche Verfügbarkeit
                    - Bisherige Erfahrungen in dem Bereich
                    Du kannst gerne mehr schreiben!
                    
                    Sobald du deine Bewerbung geschrieben hast, meldet sich die zuständige Person bei dir.
                    """,
            TicketTypeGroup.BEWERBUNG,
            List.of(EnvKey.ROLE_SUPPORTLEITUNG)
    ),
    BEWERBUNG_MODERATION(
            "bemod",
            "⚒️ Fachbereich Moderation",
            "Bewerbung Fachbereich Moderation",
            """
                    Bitte verfasse deine Bewerbung. Für deine Bewerbung ist uns Folgendes wichtig::
                    
                    - Kurze Vorstellung (Name, Alter, Pronomen, ...)
                    - Warum möchtest du Teil des Moderationsteam werden?
                    - Deine zeitliche Verfügbarkeit
                    - Bisherige Erfahrungen in dem Bereich
                    Du kannst gerne mehr schreiben!
                    
                    Sobald du deine Bewerbung geschrieben hast, meldet sich die zuständige Person bei dir.
                    """,
            TicketTypeGroup.BEWERBUNG,
            List.of(EnvKey.ROLE_MODLEITUNG, EnvKey.ROLE_SERVERLEITUNG)
    ),
    BEWERBUNG_AWARENESS(
            "beawrn",
            "💚 Fachbereich Awareness",
            "Bewerbung Fachbereich Awareness",
            """
                    Bitte verfasse deine Bewerbung. Für deine Bewerbung ist uns Folgendes wichtig::
                    
                    - Kurze Vorstellung (Name, Alter, Pronomen, ...)
                    - Warum möchtest du Teil des Awarenessteam werden?
                    - Deine zeitliche Verfügbarkeit
                    - Bisherige Erfahrungen in dem Bereich
                    Du kannst gerne mehr schreiben!
                    
                    Sobald du deine Bewerbung geschrieben hast, meldet sich die zuständige Person bei dir.
                    """,
            TicketTypeGroup.BEWERBUNG,
            List.of(EnvKey.ROLE_AWARENESSLEITUNG)
    ),
    BEWERBUNG_EVENT(
            "beevnt",
            "🎊 Fachbereich Event",
            "Bewerbung Fachbereich Event",
            """
                    Bitte verfasse deine Bewerbung. Für deine Bewerbung ist uns Folgendes wichtig::
                    
                    - Kurze Vorstellung (Name, Alter, Pronomen, ...)
                    - Warum möchtest du Teil des Eventteams werden?
                    - Deine zeitliche Verfügbarkeit
                    - Bisherige Erfahrungen in dem Bereich
                    Du kannst gerne mehr schreiben!
                    
                    Sobald du deine Bewerbung geschrieben hast, meldet sich die zuständige Person bei dir.
                    """,
            TicketTypeGroup.BEWERBUNG,
            List.of(EnvKey.ROLE_EVENTLEITUNG)
    );


    @Getter
    private final String id;
    @Getter
    private final String label;
    @Getter
    private final String selectDescription;
    @Getter
    private final String embedDescription;
    @Getter
    private final TicketTypeGroup group;
    @Getter
    private final List<Role> responsibleRoles;

    TicketType(String id, String label, String selectDescription, String embedDescription, TicketTypeGroup group, List<EnvKey> responsibleRoles){
        this.id = id;
        this.label = label;
        this.selectDescription = selectDescription;
        this.embedDescription = embedDescription;
        this.group = group;
        this.responsibleRoles = responsibleRoles.stream().map(EnvResolver::getRoleById).toList();
    }
}
