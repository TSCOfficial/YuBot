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
        addTextDisplay("-# Gemäss Sektion 5 der Discord Entwickler-Nutzungsbedingungen, dem DSGVO und revDSG");

        addTextDisplay("## Angaben zur verantwortlichen Person");

        addTextDisplay("""
                Aliz Rose Frily
                
                Discord: \\@m.nt_lizzy
                E-Mail: contact@alizrose.ch
                """);

        addTextDisplay("## Ansprechpartner:in für datenschutzrechtlche Fragen");
        addTextDisplay("Bei Fragen zum Datenschutz steht Aliz, unter den oben genannten Angaben, zur Verfügung.");

        addTextDisplay("## Verarbeitung personenbezogene Daten");
        addTextDisplay("### Datenverarbeitung");
        addTextDisplay("""
                Der YuBot verarbeitet Daten aus Benutzerprofile (Profilbild, Profilbanner, Primäre Farbe, Nickname, Name, Discord-ID, Präsenz-Status, Sprachkanal-Status, Rollen, Berechtigungen), Nachrichten (Inhalte, Discord-ID, Kanal-ID, Zeitpunkt) und Interaktionen (Slashcommands, Buttons, Modals, Contextmenus) mit dem Bot.
                Diese Daten dienen ausschliesslich für den korrekten Betrieb der vom YuBot angeboteten Funktionen.
                """);

        addTextDisplay("### Datenspeicherung");
        addTextDisplay("""
                Der Yubot speichert einige Daten, die ausschliesslich für den korrekten Betrieb des YuBots verwendet werden.
                Daten die aus der Discord API entnommen werden, werden lediglich als Referenz gespeichert. Es werden keine Nachrichteninhalte oder Ähnliches gespeichert.
                
                Gespeicherte Daten können folgende sein: Discord-Account-ID, Discord Name, Discord-Nachricht-ID, Zeitpunkt gesendeter Nachricht, Zeitpunkt bearbeiteter Nachricht, Discord-Kanal-ID. Ebenfalls werden Daten gespeichert, die durch die Benutzenden eigenständig, während Interaktionen, angegeben werden.
                Aus diesen Daten werden, für spezifische Zielgruppen, für sie Einsehbare, Aktivitätsprofile erstellt.
                """);

        addTextDisplay("### Datenlöschung");
        addTextDisplay("""
                Benutzende können für einige Daten, die im Zusammenhang mit ihrem Discord-Account stehen, eine Löschung bei der Verantwortlichen Person beantragen. Einige Daten können aufgrund der Aufbewahrungsrechtlinie, und zur legalen Absicherung unseres Teams, nicht gelöscht werden.
                """);
    }
}
