package ch.frily.yubot.container;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class StartHereContainer extends Container {
    public StartHereContainer() {
//        this.addComponent(
//                MediaGallery.of(MediaGalleryItem.fromUrl(EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getBannerUrl()))
//        );

        this.addTextDisplay(String.format("# Willkommen bei %s!", EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getName()));
        this.addTextDisplay("""
                Heyyy ihr Süßen und schön, dass ihr da seid! 🤞
                Weirdos soll ein Safe Space sein, an dem ihr euch wohlfühlen und genauso sein könnt, wie ihr seid. Macht es euch gemütlich, lernt neue Menschen kennen und genießt gemeinsam die Zeit auf unserem Server!
                """);

        this.addInvisibleSeparator(Separator.Spacing.LARGE);

        this.addTextDisplay("""
                ## Regeln und Server Team
                Damit dieser Ort für alle sicher und angenehm bleibt, gibt es natürlich ein paar Grundregeln zu beachten. Bitte schaut dafür im Server Guide bei den Regeln vorbei und lest sie euch in Ruhe durch.
                
                Direkt darunter findet ihr außerdem eine Übersicht über unser Server-Team. Dort könnt ihr sehen, aus welchen tollen Leuten unser Team besteht.
                """);

        this.addInvisibleSeparator(Separator.Spacing.LARGE);

        this.addTextDisplay(String.format("""
                ## Support & Awareness
                Wenn ihr Unterstützung benötigt, ist der %s eure erste Anlaufstelle.
                
                Fühlt ihr euch gerade nicht gut, braucht jemanden zum Reden oder möchtet euch mit einer Person aus unserem Awareness-Team austauschen, könnt ihr dort ein Awareness-Ticket öffnen. Unser Team ist immer gerne für euch da! ❤️
                
                Bei Fragen, Problemen oder wenn ihr einen möglichen Regelverstoß melden möchtet, könnt ihr stattdessen ein Support-Ticket erstellen. Nachdem ihr „Support“ ausgewählt habt, könnt ihr im nächsten Schritt das passende Thema für euer Anliegen wählen.
                """, EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_SUPPORT).getAsMention()));

        this.addInvisibleSeparator(Separator.Spacing.LARGE);

        this.addTextDisplay(String.format("""
                ## Öffnung des Servers
                Der Server ist nur geöffnet, solange ausreichend Teammitglieder\\*innen anwesend sind, die sich um euch und mögliche Anliegen kümmern können. Ob der server geöffnet oder geschlossen ist, seht ihr anhand von %s.
                
                Sollte der Server zwischenzeitlich geschlossen sein, könnt ihr euch trotzdem in %s die Zeit vertreiben und gemeinsam versuchen, bis ins Unendliche zu zählen. 1️⃣6️⃣1️⃣
                Bitte achtet auch dort weiterhin auf die geltenden Regeln.
                """, EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_SERVERGESCHLOSSEN).getAsMention(),
                EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_COUNTING).getAsMention()));

        this.addInvisibleSeparator(Separator.Spacing.LARGE);

        this.addTextDisplay(String.format("""
                ## Events
                Da wir diesen Ort gemeinsam mit Leben füllen möchten, dürfen natürlich auch Events nicht fehlen! ✨
                
                Unsere größeren Events finden auf der %s statt. Dort könnt ihr beitreten, zuhören und gemeinsam an Events teilnehmen, die von einer Person aus unserem Team moderiert werden.
                
                Für kleinere Aktionen können außerdem zeitweise separate Text- oder Voice-Channels erstellt werden. Dort könnt ihr euch austauschen, mitmachen und gemeinsam eine schöne Zeit verbringen. ☎️
                """, EnvResolver.getChannelById(StageChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_LIVESTAGE).getAsMention()));

        this.addInvisibleSeparator(Separator.Spacing.LARGE);

        this.addTextDisplay(String.format("""
                ## Communityfloor
                Dieser Bereich gehört euch ˙ᵕ˙
                ⤷ Wenn der Server geöffnet ist könnt ihr hier über alle möglichen Themen schreiben und sprechen.
                
                %s ▸ hier könnt ihr miteinander schreiben
                %s ▸ hier könnt ihr euch einander vorstellen
                %s ▸ hier könnt ihr Yu Fanart aller Art reinschicken und bewundern ✏️
                %s ▸ hier könnt ihr euch über Musik unterhalten 🎼
                %s ▸ hier könnt ihr eure eigenen Songtexte und Ideen posten 🎵
                %s ▸ hier könnt ihr euch über queere Themen austauschen und euch gegenseitig supporten ❤️
                
                ### ⪼ Foren
                %s ▸ hier könnt ihr dem Serverteam feedback geben ✉️
                %s ▸ hier könnt ihr euch als Konzertbegleitung für andere anbieten, oder nach solcher fragen und euch zusammentun
                %s ▸ hier könnt ihr euch über Konzerte oder Festivals, auf denen ihr Yu sehen könnt, unterhalten
                
                ### ⪼ Voice Channels
                Euch stehen mehrere Voice Channels zur Verfügung, einige davon haben eine Maximalanzahl an User\\*innen, damit diese nicht so unübersichtlich werden.
                Zusätzlich gibt es die Möglichkeit "Join to create", mit der ihr selbst einen Voice Channel erstellen könnt, indem ihr dem Voice %s beitretet. Dort könnt ihr den Namen und die maximale Anzahl der User\\*innen einstellen. Außerdem können euch andere Yu Fans Anfragen schicken, um diesen Vc´s joinen zu können. So könnt ihr euch in kleineren Gruppen zusammentun. ☎️
                
                Nicht wundern wenn von Zeit zur Zeit mal ein\\*e Moderator\\*in oder ein\\*e Supporter\\*in dem Voicechat joint und nach dem rechten sieht.
                """,
                EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_LOBBY).getAsMention(),
                EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_STELLDICHVOR).getAsMention(),
                EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_FANART).getAsMention(),
                EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_MUSICDISCUSSION).getAsMention(),
                EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_LYRICLAB).getAsMention(),
                EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_QUEERSPACE).getAsMention(),
                EnvResolver.getChannelById(ForumChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_FEEDBACK).getAsMention(),
                EnvResolver.getChannelById(ForumChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_KONZERTBEGLEITUNG).getAsMention(),
                EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_YUKONZERTFESTIVALS).getAsMention(),
                EnvResolver.getChannelById(VoiceChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_JOINTOCREATE).getAsMention()
            ));

        this.addInvisibleSeparator(Separator.Spacing.LARGE);

        this.addTextDisplay("-# Das YuTeam wünscht euch viel spass! ❤️");
    }
}
