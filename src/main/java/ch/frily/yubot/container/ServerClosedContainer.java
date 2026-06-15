package ch.frily.yubot.container;

import ch.frily.yubot.Client;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.FileUpload;

import java.nio.file.Path;

public class ServerClosedContainer extends Container {

    public ServerClosedContainer(){
        this.addComponent(Section.of(
                Thumbnail.fromUrl(EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getIconUrl()),
                TextDisplay.of("## 🔒 Server geschlossen"),
                TextDisplay.of("Der Server ist momentan geschlossen. Aber keine Sorge - sobald wir wieder öffnen, wirst du Benachrichtigt ✨")
        ));
        this.addComponent(Separator.createInvisible(Separator.Spacing.SMALL));
        this.addComponent(TextDisplay.of("In der zwischenzeit kannst du dir Yu's Inhalte anschauen:"));
        this.addComponent(ActionRow.of(
                Button.of(ButtonStyle.LINK, "https://www.instagram.com/einfachyu/", "Instagram", Emoji.fromCustom("instagram", 1513868079711649805L, false)),
                Button.of(ButtonStyle.LINK, "https://open.spotify.com/intl-de/artist/3fePw6n7ygV222wnREArp6?si=bO9Y61msSLShf46qbsDPtQ", "Spotify", Emoji.fromCustom("spotify", 1513868764209348669L, false)),
                Button.of(ButtonStyle.LINK, "https://tiktok.com/@einfachyu", "Tiktok", Emoji.fromCustom("tiktok", 1514163991331475526L, false)),
                Button.of(ButtonStyle.LINK, "https://www.twitch.tv/einfachyu", "Twitch", Emoji.fromCustom("twitch", 1513866550615085196L, false)),
                Button.of(ButtonStyle.LINK, "https://www.youtube.com/channel/UC-aJNxT5dDuRZrc8MTNo5ag", "YouTube", Emoji.fromCustom("youtube", 1513868081653354666L, false))

        ));
        this.addComponent(Separator.createInvisible(Separator.Spacing.SMALL));
        this.addComponent(Separator.createDivider(Separator.Spacing.LARGE));
        this.addComponent(TextDisplay.of("-# **Wieso ist der Server geschlossen?**"));
        this.addComponent(TextDisplay.of("-# Zum Schutz des Safespaces, und damit auch der Community, schliesst sich der Server automatisch, sobald es keine aktive Moderator*innen mehr gibt."));
    }
}
