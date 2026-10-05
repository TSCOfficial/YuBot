package ch.frily.yubot.embed;

import ch.frily.yubot.util.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public interface IEmbed {

    // Author
    default String getAuthorName(){
        return "";
    }

    default String getAuthorUrl(){
        return "";
    }

    default String getAuthorIconUrl(){
        return "";
    }

    // Content
    default String getTitle(){
        return "";
    }

    default String getTitleUrl(){
        return "";
    }

    default String getDescription(){
        return "";
    }

    default List<Field> getFields() {
        return new ArrayList<>();
    }

    default java.awt.Color getColor(){
        return Color.LIGHT_GRAY;
    }

    default String getFooterText(){
        return "";
    }

    default String getFooterIconUrl(){
        return "";
    }

    default Instant getTimestamp() {
        return new Date().toInstant();
    }

    default MessageEmbed build() {
        EmbedBuilder builder = new EmbedBuilder();

        // Author
        if (!getAuthorName().isBlank() && !getAuthorUrl().isBlank() && !getAuthorIconUrl().isBlank()) {
            builder.setAuthor(getAuthorName(), getAuthorUrl(), getAuthorIconUrl());
        } else if (!getAuthorName().isBlank() && !getAuthorUrl().isBlank()) {
            builder.setAuthor(getAuthorName(), getAuthorUrl());
        } else if (!getAuthorName().isBlank() && !getAuthorIconUrl().isBlank()) {
            builder.setAuthor(getAuthorName(), null, getAuthorIconUrl());
        } else if (!getAuthorName().isBlank()) {
            builder.setAuthor(getAuthorName());
        }

        // Title
        if (!getTitle().isBlank() && !getTitleUrl().isBlank()) {
            builder.setTitle(getTitle(), getTitleUrl());
        } else if (!getTitle().isBlank()) {
            builder.setTitle(getTitle());
        }

        // Description
        if (!getDescription().isBlank()) {
            builder.setDescription(getDescription());
        }

        // Fields
        if (getFields().isEmpty()) {
            for (Field field : getFields()) {
                if (Objects.equals(field.getName(), " ") && Objects.equals(field.getValue(), " ")) {
                    builder.addBlankField(field.isInline());
                } else {
                    builder.addField(field);
                }
            }
        }

        // Color
        if (getColor() != null) {
            builder.setColor(getColor());
        }

        // Footer
        if (!getFooterText().isBlank() && !getFooterIconUrl().isBlank()) {
            builder.setFooter(getFooterText(), getFooterIconUrl());
        } else if (!getFooterText().isBlank()) {
            builder.setFooter(getFooterText());
        }

        // Timestamp
        if (getTimestamp() != null) {
            builder.setTimestamp(getTimestamp());
        }

        return builder.build();
    }
}
