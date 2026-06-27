package ch.frily.yubot.container;

import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import org.w3c.dom.Text;

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
                TextDisplay.of("### Nummer gegen Kummer"),
                TextDisplay.of("<:call:1520537165690437652> 116111 oder 0800 111 0 333"),
                TextDisplay.of("-# Montag bis Samstag, 14-20 Uhr")
        );
        this.addSection(
                Button.link("https://www.telefonseelsorge.de/", "TelefonSeelsorge DE"),
                TextDisplay.of("### TelefonSeelsorge"),
                TextDisplay.of("<:call:1520537165690437652> 0800 111 0 111 oder 0800 111 0 222"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://www.anrufen-hilft.de/", "Anrufen Hilft"),
                TextDisplay.of("### Anrufen Hilft - Hilfe bei sexuellem Missbrauch"),
                TextDisplay.of("<:call:1520537165690437652> 0800 22 55 530"),
                TextDisplay.of("-# Montag bis Freitag")
        );
        this.addSection(
                Button.link("https://www.hilfetelefon.de/", "Hilfetelefon"),
                TextDisplay.of("### Hilfetelefon - Gewalt gegen Frauen"),
                TextDisplay.of("<:call:1520537165690437652> 08000 116 016"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://queermed-deutschland.de/", "Queermed DE"),
                TextDisplay.of("### Queermed DE - Liste mit Queer-freundlichen Arztpraxen & Therapeuten")
        );

        this.addLineSeparator(Separator.Spacing.LARGE);

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
                TextDisplay.of("### Ö3 - Kummernummer"),
                TextDisplay.of("<:call:1520537165690437652> 116 123"),
                TextDisplay.of("-# Täglich von 16-24 Uhr")
        );
        this.addSection(
                Button.link("https://www.telefonseelsorge.at/", "TelefonSeelsorge AT"),
                TextDisplay.of("### Telefonseelsorge"),
                TextDisplay.of("<:call:1520537165690437652> 124"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://www.rataufdraht.at/telefonberatung/", "Rat auf Draht AT"),
                TextDisplay.of("### Rat auf Draht"),
                TextDisplay.of("<:call:1520537165690437652> 147"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://queermed.at/", "Queermed AT"),
                TextDisplay.of("### Queermed AT - Liste mit Queer-freundlichen Arztpraxen & Therapeuten")
        );

        this.addLineSeparator(Separator.Spacing.LARGE);

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
                TextDisplay.of("### Die Dargebotene Hand"),
                TextDisplay.of("<:call:1520537165690437652> 143"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://www.147.ch/", "Pro Juventute"),
                TextDisplay.of("### Pro Juventute"),
                TextDisplay.of("<:call:1520537165690437652> 147"),
                TextDisplay.of("-# Jederzeit")
        );
        this.addSection(
                Button.link("https://mycheckpoint.ch/", "Checkpoint"),
                TextDisplay.of("### Checkpoint - Gesundheitszentrum für die LGBTQ-Community")
        );
    }
}
