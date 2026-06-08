package ch.frily.yubot.embed;

import ch.frily.yubot.feature.TicketType;
import ch.frily.yubot.feature.TicketTypeGroup;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class PanelEmbed implements IEmbed {
    @Override
    public String getAuthorName() {
        return EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getName();
    }

    @Override
    public String getAuthorIconUrl() {
        return EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getIconUrl();
    }

    @Override
    public String getTitle() {
        return "Ticketsystem";
    }

    @Override
    public String getDescription() {
        return "Wähle den passenden Kontaktbereich aus, drücke den dazugehörigen Button und wähle anschliessend die gewünschte Kategorie aus.";
    }

    @Override
    public List<Field> getFields() {
        return Arrays.stream(TicketTypeGroup.values()).map(typeGroup -> {
            StringBuilder description = new StringBuilder();
            description.append(typeGroup.getDescription());
            description.append("\n**Verfügbare Kategorien:**\n");

            Arrays.stream(TicketType.values()).filter(type -> type.getGroup() == typeGroup).forEach(type -> {
                description.append("- ").append(type.getLabel()).append(" - ").append(type.getSelectDescription()).append("\n");
            });


            return new Field(typeGroup.getLabel(), description.toString(), false);
        }).toList();
    }

    @Override
    public String getFooterText() {
        return "XYZCraft Support";
    }

    @Override
    public Instant getTimestamp() {
        return null;
    }
}
