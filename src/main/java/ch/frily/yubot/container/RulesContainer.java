package ch.frily.yubot.container;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
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
        this.addComponent(MediaGallery.of(MediaGalleryItem.fromFile(FileUpload.fromData(new File("src/main/resources/img/regelwerk-header.png")))));
        this.addTextDisplay(Util.format("## Serverregeln von {}", GUILD.getName()));
        this.addTextDisplay("-# *Stand 15. Juni 2026*");

        this.addTextDisplay(Util.format("Dies sind die allgemein gültigen Regeln des öffentlichen Community-Servers: \"{}\". Sie sind entsprechend des Reglements verbindlich für alle Mitglieder des Discord-Servers.", GUILD.getName()));
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
                - §7 Umgang mit Regelbrüchen
                - §8 Server-Staff
                - §9 Vergabe und Entzug erweiterter Rechte
                """);

        this.buildContainerWithCurrentComponents();

        this.addTextDisplay("**§1 Zielsetzung**");
        this.addTextDisplay(Util.format("""
                       {} ist der offizielle deutschsprachige Community-Server von Yu. Auf ihm soll ein gemütliches, friedliches und freundschaftliches Miteinander ermöglicht werden.
                        """, GUILD.getName())
        );

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        this.addTextDisplay("**§2 Rechte und Pflichten einer Mitgliedschaft**");
        this.addSection(
                Button.link("https://discord.com/terms", "Discord ToS"),
                TextDisplay.of("""
                        ¹ Das Beitreten und Verweilen auf dem Servers steht jeder Person frei zur Wahl, welche 16 oder Älter ist. Ausnahmen bezüglich des Alters sind gemäss der Liste mit weltweiten Angaben zum Mindestalter vom Discord ToS zu entnehmen.
                        """)
        );
        this.addTextDisplay("""
                ² Der Nickname muss mit einer Standart-QWERTZ-Tastatur schreibbar sein. Außerdem darf er §3 nicht verletzen. Nicknames dienen der alltäglichen Identifizierung von Anderen und sollten deswegen mögllichst einzigartig sein. Entspricht ein Nickname nicht den genannten Regeln, wird er vom Serverteam geändert.
                """
        );

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        this.addTextDisplay("**§3 Recht des Anderen**");
        this.addTextDisplay("""
                        
                        Auf diesem Server hat jede Person das Recht, würdevoll und respektvoll behandelt zu werden. Belästigung, Sexismus, Rassismus, Volksverhetzung, Queerfeindlichkeit und Diskriminierung werden nicht toleriert. (Strafen siehe §7)
                        """
        );

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        this.addTextDisplay("**§4 Inhalte für Erwachsene**");
        this.addTextDisplay("""
                        NSFW- oder obszöne Inhalte sind nicht erwünscht. Dazu zählen Texte, Bilder oder Links mit Nacktheit, Sex, schwerer Gewalt oder anderen grafisch verstörenden Inhalten.
                        """
        );

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        this.addTextDisplay("**§5 Voicechats**");
        this.addTextDisplay("""
                        ¹ Jede Person hat die Pflicht, sich in Voicechats respektvoll und maßvoll zu verhalten und Andere nicht zu stören. In Voicechats ist es untersagt, jegliche Formen von Drogen (auch Alkohol, Vapes oder Zigaretten) zu konsumieren.
                        """
        );
        this.addTextDisplay("""
                ² Auch Regelverstöße in Sprachchats führen zu Konsequenzen (siehe §7). Moderator\\*innen und Supporter\\*innen schauen alle halbe Stunde in den Voicechats nach dem Rechten und dem Wohlbefinden.
                """
        );

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        this.addTextDisplay("**§6 Spam**");
        this.addTextDisplay("""
                        Spam ist nicht erwünscht. Formen von Spam sind:
                        1. Missbrauch von Pings
                        2. Missbrauch von Kanälen/Kanalfunktionen
                        3. Übermäßiges, hochfrequentiertes Schreiben von Nachrichten
                        4. NSFW Inhalte jeglicher Art
                        5. Scamversuche
                        6. nicht authorisierte Fremd- & Eigenwerbung
                        7. Belästigung anderer Servermitglieder a) auf dem Server b) in den DMs
                        8. Regelwidrige Nicknames
                        
                        Regelbrüche werden nach §7 geahndet.
                        """
        );

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        this.addTextDisplay(" **§6a Bewerbung anderer Discord Server**");
        this.addTextDisplay("""
                        Wer andere Mitglieder über diesen Server dazu bewegt, auf andere Discord Server zu joinen, wird zum Schutz von Minderjährigen gebannt, da diese Server nicht von uns moderiert werden und wir nicht gewährleisten können, dass diese auch sicher sind.
                        """
        );

        this.buildContainerWithCurrentComponents();

        this.addTextDisplay("**§7 Umgang mit Regelbrüchen**");
        this.addTextDisplay("""
                        ¹ *Von regulären Mitgliedern*
                        Bei Regelbrüchen entscheiden die Moderatoren, ob ein Regelbruch vorliegt und ob bzw. welche Strafe angemessen ist. Mögliche Strafen sind u.a.:
                        - Timeout
                        - Kick
                        - Bann
                        """
        );
        this.addTextDisplay("""
                ² *Von Server-Staff*
                Ein Mitglied des Server-Staffs ist zu entlassen, wenn die Person
                - unbegründete Strafen verhängt
                - gegen §3, §4, §6 oder §9 verstößt
                - nach eigennützigen Motiven als Server-Staff agiert und damit anderen schadet
                - Persönliche Bekannte bevorzugt
                """
        );

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        this.addTextDisplay("**§8 Server-Staff**");
        this.addTextDisplay("""
                Mitglieder des Server-Staff sind verpflichtet, sich den Serverregeln entsprechend zu verhalten und als Vorbild vorrauszugehen. Sie haben im Zweifelsfall immer das letzte Wort, bis ein Mitglied höheren Ranges etwas anderes sagt.
           
                Jedes der unten genannten Teams verfügt über eine oder mehrere Leitungspersonen, die mittels eigener Rolle besonders gekennzeichnet sind. Diese verteilen die Aufgaben im Team und sind für ihre Teammitglieder verantwortlich.
                - Event-Team : Organisiert die Events auf dem Server.
                - Awareness-Team : Hilfe bei persönlichen Problemen und Konflikten.
                - Support\\*innen : Sorgen für Einhaltung der Regeln, beantworten Tickets. Können timeouten.
                - Moderator\\*innen : Wie Supporter\\*innen, aber entscheiden und verhängen Strafen. Können zusätzlich kicken und bannen.
                - Development-Team : Kümmern sich um alles technische an Server und Bots.
                - Serverleitung : Setzt den Server im Auftrag von Yu um.
                - Owner : Besitzt den Servers und die Ownerrechte, höchste Instanz.
                """
        );

        this.addInvisibleSeparator(Separator.Spacing.SMALL);

        this.addTextDisplay("**§9 Vergabe und Entzug erweiterter Rechte**");
        this.addTextDisplay("""
                ¹ Die Vergabe von erweiterten Rechten erfolgt in der Regel direkt durch den Vorgesetzten und immer in der Annahme, dass mit ihnen verantwortungsvoll und im Sinne des Servers umgegangen wird.
                """
        );
        this.addTextDisplay("""
                ² Ist die verantwortungsvolle Benutzung der Rechte aus Sicht des Serverowners, der Serverleitung oder der zuständigen Teamleitung nicht gegeben, können diese Rechte jederzeit wieder entzogen werden.
                """
        );

        this.addLineSeparator(Separator.Spacing.LARGE);

        this.addTextDisplay("-# Die Serverleitung behält sich das Recht vor, diese Regeln jederzeit, auch ohne Ankündigung, zu ändern. Solange man Mitglied dieses Servers ist, gelten die Regeln als aktzeptiert.");
    }
}
