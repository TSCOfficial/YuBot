package ch.frily.yubot.container.guildinfo;

import ch.frily.yubot.container.Container;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

public class MentalHealthHelpContainer extends Container {

    // Basiert auf https://ptb.discord.com/channels/1045025807065698314/1516042711273046087/1520524774067933264
    public MentalHealthHelpContainer() {
        this.addTextDisplay("# Seelensorge und Beratungsstellen");
        this.addTextDisplay("Hier findest du eine Liste an verschiedenen Seelensorge-Hotlines und Webseiten, oder Beratungsstellen.");
        this.addTextDisplay("-# Unser Awareness-Team versucht deren bestes zu geben, um dir zu helfen - du kannst sie direkt in <#1435267124888338432> kontaktieren. Unser Team ist jedoch nicht darauf trainiert.");

        this.addInvisibleSeparator(Separator.Spacing.LARGE);

        this.addTextDisplay("## \uD83C\uDDE9\uD83C\uDDEA Deutschland");
        this.addTextDisplay("""
                ### Notfallnummern
                Europäischer Notruf: 112
                Polizei: 110
                Rettungsdienst & Feuerwehr: 112
                Ärzte: 116117
                """);
        this.addSection(
                Button.link("https://www.nummergegenkummer.de/", "Nummer gegen Kummer"),
                TextDisplay.of("""
                        ### Nummer gegen Kummer
                        Kostenlose, anonyme Beratung für Kinder und Jugendliche bei allen Sorgen und Problemen.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 116111 oder 0800 111 0 333"),
                TextDisplay.of("-# Montag bis Samstag, 14-20 Uhr")
        );
        this.addSection(
                Button.link("https://www.telefonseelsorge.de/", "TelefonSeelsorge DE"),
                TextDisplay.of("""
                        ### TelefonSeelsorge
                        Seelsorge und Krisenberatung für Menschen jeden Alters - telefonisch, per Chat oder E-Mail.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 0800 111 0 111 oder 0800 111 0 222"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://www.anrufen-hilft.de/", "Anrufen Hilft"),
                TextDisplay.of("""
                        ### Anrufen Hilft
                        Hilfe bei sexuellem Missbrauch.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 0800 22 55 530"),
                TextDisplay.of("-# Mo., Mi., Fr. 9-14 Uhr & Di. Do. 15-20 Uhr")
        );
        this.addSection(
                Button.link("https://www.hilfetelefon.de/", "Hilfetelefon"),
                TextDisplay.of("""
                        ### Hilfetelefon
                        Beratung für von Gewalt betroffene Frauen und deren Angehörige.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 08000 116 016"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://queermed-deutschland.de/", "Queermed DE"),
                TextDisplay.of("""
                        ### Queermed DE
                        Liste mit Queer-freundlichen Arztpraxen & Therapeuten.
                        """)
        );
        this.addSection(
                Button.link("https://krisenchat.de/", "krisenchat"),
                TextDisplay.of("""
                        ### Krisenchat
                        Chat-Beratung für Kinder, Jugendliche & junge Erwachsene (bis 25).
                        """),
                TextDisplay.of("<:phone:1526301451733696752> WhatsApp/SMS: +49 1573 5998143"),
                TextDisplay.of("-# Jederzeit")
        );

        this.buildContainerWithCurrentComponents();

        this.addTextDisplay("## \uD83C\uDDE6\uD83C\uDDF9 Österreich");

        this.addTextDisplay("""
                ### Notfallnummern
                Europäischer Notruf: 112
                Polizei: 133
                Feuerwehr: 122
                Rettungsdienst: 144
                """);
        this.addSection(
                Button.link("https://oe3.orf.at/kummernummer/stories/2712988/", "Ö3"),
                TextDisplay.of("""
                        ### Ö3 - Kummernummer
                        Erstanlaufstelle für Menschen in persönlichen Notlagen.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 116 123"),
                TextDisplay.of("-# Täglich von 16-24 Uhr")
        );
        this.addSection(
                Button.link("https://www.telefonseelsorge.at/", "TelefonSeelsorge AT"),
                TextDisplay.of("""
                        ### TelefonSeelsorge
                        Vertraulicher Notrufdienst für Menschen in Krisensituationen.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 142"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://www.rataufdraht.at/telefonberatung/", "Rat auf Draht"),
                TextDisplay.of("""
                        ### Rat auf Draht
                        Beratung für Kinder, Jugendliche und junge Erwachsene bei allen Problemen.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 147"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://queermed.at/", "Queermed AT"),
                TextDisplay.of("""
                        ### Queermed AT
                        Liste mit Queer-freundlichen Arztpraxen & Therapeuten.
                        """)
        );
        this.addSection(
                Button.link("https://www.frauenhelpline.at/", "Frauenhelpline"),
                TextDisplay.of("""
                        ### Frauenhelpline gegen Gewalt
                        Beratung für gewaltbetroffene Frauen und deren Angehörige.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 0800 222 555"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://maennernotruf.at/", "Männernotruf"),
                TextDisplay.of("""
                        ### Männernotruf
                        Beratung für Männer in Krisensituationen.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 0800 246 247"),
                TextDisplay.of("-# Jederzeit")
        );

        this.buildContainerWithCurrentComponents();

        this.addTextDisplay("## \uD83C\uDDE8\uD83C\uDDED Schweiz");

        this.addTextDisplay("""
                ### Notfallnummern
                Europäischer Notruf: 112
                Polizei: 117
                Feuerwehr: 128
                Rettungsdienst: 144
                Vergiftung: 145 
                """);
        this.addSection(
                Button.link("https://www.143.ch/", "Dargebotene Hand"),
                TextDisplay.of("""
                        ### Die Dargebotene Hand
                        Sorgentelefon für Menschen in schwierigen Lebenslagen - unabhängig von Alter und Thema.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 143"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://www.147.ch/", "Pro Juventute"),
                TextDisplay.of("""
                        ### Pro Juventute
                        Beratung und Nothilfe für Kinder und Jugendliche.
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 147"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://mycheckpoint.ch/", "Checkpoint"),
                TextDisplay.of("""
                        ### Checkpoint
                        Gesundheitszentrum für die LGBTQ-Community.
                        """)
        );
        this.addSection(
                Button.link("https://www.opferhilfe-schweiz.ch/de/", "Opferhilfe Schweiz"),
                TextDisplay.of("""
                        ### Opferhilfe Schweiz
                        Für Betroffene von Gewalt (und Angehörige).
                        """),
                TextDisplay.of("<:phone:1526301451733696752> 142"),
                TextDisplay.of("-# Jederzeit - keine Notrufnummer, bei akuter Gefahr 117/144 wählen")
        );
    }
}
