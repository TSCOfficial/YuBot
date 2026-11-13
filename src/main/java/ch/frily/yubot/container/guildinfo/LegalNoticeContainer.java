package ch.frily.yubot.container.guildinfo;

import ch.frily.yubot.container.*;
import ch.frily.yubot.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class LegalNoticeContainer extends Container {

    // https://www.legalforge.app/blog/privacy-policy-for-discord-bot
    public LegalNoticeContainer() {
        addTextDisplay("# Datenschutzangaben");
        addTextDisplay("-# Gemäss Sektion 5 der Discord Entwickler-Nutzungsbedingungen");

        addTextDisplay("## Angaben zur verantwortlichen Person");

        addTextDisplay("""
                Aliz Rose Frily
                
                Discord: \\@m.nt_lizzy (primär)
                E-Mail: contact@alizrose.ch
                """);

        addTextDisplay("## Ansprechpartner:in für datenschutzrechtlche Fragen");
        addTextDisplay("Bei Fragen zum Datenschutz steht Aliz, unter den oben genannten Angaben, zur Verfügung.");

        addTextDisplay("## Verarbeitung personenbezogene Daten");
        addTextDisplay("### Datenverarbeitung");
        addTextDisplay("""
                Der YuBot entnimmt diverse Daten aus der offiziellen Discord API. Dazu gehören Daten aus Benutzerprofile (Profilbild, Profilbanner, Primäre Rollenfarbe, Nickname, Name, Discord-Account-ID, Präsenz-Status, Sprachkanal-Status, Rollen), Nachrichten (Inhalte, Discord-Nachricht-ID, Discord-Kanal-ID, Zeitpunkt) und Interaktionen (Slashcommands, Buttons, Modals, Contextmenus), sowie den bei den Interaktionen angegebenen oder ausgewählten Optionen.
                Diese Daten dienen ausschliesslich für den korrekten Betrieb der vom YuBot angeboteten Funktionen.
                """);

        addTextDisplay("### Datenspeicherung");
        addTextDisplay("""
                Der Yubot speichert bei der verarbeitung einige Daten, die ausschliesslich für den korrekten Betrieb des YuBots verwendet werden.
                Persistente Daten, die Discord selbst speichert und aus der Discord API entnommen werden können, werden lediglich als Referenz gespeichert. Es werden keine Nachrichteninhalte, Medien/Anhänge oder Ähnliches gespeichert.
                
                Gespeicherte Daten können folgende sein: Discord-Account-ID, Discord Name, Discord-Nachricht-ID, Zeitpunkt gesendeter Nachricht, Zeitpunkt bearbeiteter Nachricht, Discord-Kanal-ID. Ebenfalls werden Daten gespeichert, die durch die Benutzenden eigenständig, während Interaktionen, angegeben werden.
                Aus diesen Daten werden, für spezifische Zielgruppen, für sie Einsehbare, Aktivitätsprofile erstellt (Activemod).
                
                Benutzende können eine Dossier ihrer gespeicherten Daten bei der Verantwortlichen Person anfragen. In diesem Dossier sind alle Daten vorhanden, welche im direkten zusammenhang mir ihrem Discord-Account zusammenhängt. Das zusammentragen der für das Dossier benötigte Daten, kann einige Tage dauern.
                """);

        addTextDisplay("### Datenlöschung");
        addTextDisplay("""
                Benutzende können für einige Daten, die im Zusammenhang mit ihrem Discord-Account stehen, eine Löschung bei der Verantwortlichen Person beantragen. Einige Daten können aufgrund der Aufbewahrungsrechtlinie, und zur legalen Absicherung unseres Teams, nicht gelöscht werden.
                """);
        addTextDisplay("## Anwendbares Recht und Gerichtsstand");
        addTextDisplay("Es gilt ausschliesslich Schweizer Recht. Das zuständige Regionalgericht sowie die Schlichtungsbehörde ist Emmental-Oberaargau.");

        addTextDisplay("## Nutzungsbestimmungen");
        addTextDisplay("""
                Die Nutzung des YuBots ist nur für Personen gestattet, welche auf dem Discord-Server “Weirdos ✨️” sind und die Regeln des Discord-Servers, sowie den Discord ToS, einhalten.
                Die Nutzung der vom YuBot angebotenen Services dürfen ausschliesslich für die jeweils beschriebene bzw. angedachte Nutzung verwendet werden. Das Ausnutzen von allfälligen Sicherheitslücken, Berechtigungsfehlern, oder weiteren unbeabsichtigten Randfällen ist verboten. Jegliche Sicherheitslücke, Berechtigungsfehlern, Systemfehler oder unbeabsichtigte (Re-)Aktionen sind der Verantwortlichen Person umgehend zu melden.
                """);
        addTextDisplay("-# Vom 3. November 2026 (Stand 11. August 2026)");
    }
}
