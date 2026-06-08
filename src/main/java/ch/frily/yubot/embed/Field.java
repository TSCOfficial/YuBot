package ch.frily.yubot.embed;

import net.dv8tion.jda.api.entities.MessageEmbed;

public class Field extends MessageEmbed.Field {

    /**
     * Blank embed field
     * @param inline Field inline state
     */
    public Field(boolean inline){
        super(null, null, inline);
    }

    /**
     * Embed Field
     * @param name Field name
     * @param value Field value
     * @param inline Field inline state
     */
    public Field(String name, String value, boolean inline) {
        super(name, value, inline);
    }

}
