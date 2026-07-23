package ch.frily.yubot.embed;

import ch.frily.yubot.util.Color;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public interface IEmbed {

    // Author
    default String getAuthorName(){
        return null;
    };

    default String getAuthorUrl(){
        return null;
    };

    default String getAuthorIconUrl(){
        return null;
    };

    // Content
    default String getTitle(){
        return null;
    };

    default String getTitleUrl(){
        return null;
    };

    default String getDescription(){
        return null;
    };

    default List<Field> getFields() {
        return null;
    };

    default java.awt.Color getColor(){
        return Color.LIGHT_GRAY;
    };

    default String getFooterText(){
        return null;
    };

    default String getFooterIconUrl(){
        return null;
    };

    default Instant getTimestamp() {
        return new Date().toInstant();
    }

    default MessageEmbed build() {
        EmbedBuilder builder = new EmbedBuilder();

        // Author
        if (getAuthorName() != null && getAuthorUrl() != null && getAuthorIconUrl() != null) {
            builder.setAuthor(getAuthorName(), getAuthorUrl(), getAuthorIconUrl());
        } else if (getAuthorName() != null && getAuthorUrl() != null) {
            builder.setAuthor(getAuthorName(), getAuthorUrl());
        } else if (getAuthorName() != null && getAuthorIconUrl() != null) {
            builder.setAuthor(getAuthorName(), null, getAuthorIconUrl());
        } else if (getAuthorName() != null) {
            builder.setAuthor(getAuthorName());
        }

        // Title
        if (getTitle() != null && getTitleUrl() != null) {
            builder.setTitle(getTitle(), getTitleUrl());
        } else if (getTitle() != null) {
            builder.setTitle(getTitle());
        }

        // Description
        if (getDescription() != null) {
            builder.setDescription(getDescription());
        }

        // Fields
        if (getFields() != null) {
            for (Field field : getFields()) {
                if (field.getName() == " " && field.getValue() == " ") {
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
        if (getFooterText() != null) {
            builder.setFooter(getFooterText(), getFooterIconUrl());
        }

        // Timestamp
        if (getTimestamp() != null) {
            builder.setTimestamp(getTimestamp());
        }

        return builder.build();
    }
}
