package ch.frily.yubot.embed;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;

import java.awt.*;
import java.util.List;

public class ServerClosedEmbed implements IEmbed {

    @Override
    public String getTitle() {
        return "Server geschlossen";
    }

    @Override
    public String getDescription() {
        return "Der Server ist momentan geschlossen. Sobald wir wieder öffnen, wirst du Benachrichtigt ✨";
    }

    @Override
    public List<Field> getFields() {
        return List.of(
                new Field(
                        "Wieso ist der Server geschlossen?",
                        "Zum schutz des Safespaces dieser Community, schliesst sich der Server automatisch sobald es keine aktive Moderator\\*innen mehr gibt.",
                        false)
        );
    }

    @Override
    public Color getColor() {
        return new ch.frily.yubot.util.Color("c01d00").get();
    }

    @Override
    public String getFooterText() {
        return EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getName();
    }

    @Override
    public String getFooterIconUrl() {
        return EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getIconUrl();
    }
}
