package ch.frily.yubot.container;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;

public class RulesContainer extends Container {

    private static final Guild GUILD = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);

    public RulesContainer() {
        this.addComponent(MediaGallery.of(MediaGalleryItem.fromFile(
                FileUpload.fromData(getClass().getResourceAsStream("/img/regelwerk-header.png"), "regelwerk-header.png"))));
        this.addTextDisplay(String.format("## Serverregeln von %s", GUILD.getName()));
        this.addTextDisplay("-# *Vom 15. Juni 2026 (Stand 11. August 2026)*");

        this.addTextDisplay(String.format("Dies sind die allgemein gültigen Regeln des öffentlichen Community-Servers: \"%s\". Sie sind entsprechend des Reglements verbindlich für alle Mitglieder des Discord-Servers.", GUILD.getName()));
        this.addInvisibleSeparator(Separator.Spacing.LARGE);
        
        this.addTextDisplay("### Inhaltsverzeichnis");
        this.addTextDisplay("""
                - §1 Zielsetzung
                - §2 Rechte und Pflichten einer Mitgliedschaft
                - §3 Recht des Anderen
                - §4 Inhalte für Erwachsene
                - §5 Voicechats
                - §6 Spam
                - §6a Bewerbung anderer Discord Server
                - §6b Gruppierungen und Zugehörigkeitsmerkmale
                - §7 Umgang mit Regelbrüchen
                - §8 Server-Staff
                - §9 Vergabe und Entzug erweiterter Rechte
                - §10 Datenschutzgrundverordnung (DSGVO)
                - §11 Zusäzliche Werke
                """);

        this.addSection(Button.link("https://doc.einfachyu.de/share/3od59b6uhi/p/serverregeln-hhcK6YDHIr", "Serverregeln"),
                TextDisplay.of("**Sieh dir unsere Serverregeln direkt auf unserem DocMost an.**")
                );

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        this.addLineSeparator(Separator.Spacing.LARGE);

        this.addTextDisplay("-# Die Serverleitung behält sich das Recht vor, diese Regeln jederzeit, auch ohne Ankündigung, zu ändern. Solange man Mitglied dieses Servers ist, gelten die Regeln als aktzeptiert.");
    }
}
