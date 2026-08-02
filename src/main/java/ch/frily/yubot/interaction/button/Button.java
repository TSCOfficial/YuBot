package ch.frily.yubot.interaction.button;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public abstract class Button implements IButton{
    Map<String, String> arguments = new TreeMap<>();

    /**
     * Get the ID of the button
     * <p>This method uses the defined-ID from {@link #defineId()} and populates the arguments on the ID.
     * <pre><code>
     *     my-btn-id?arg1=value1&arg2=value2
     * </code></pre></p>
     * To retrieve and dispatch the button, split at <code>?</code>.
     * @return
     */
    public String getId(){
        String idWithArgs = defineId() + "?" + arguments.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
        String uniqueifier = "#" + ThreadLocalRandom.current().nextInt(1, 100);
        return idWithArgs + uniqueifier;
    };

    public void addArgument(String key, String value){
        arguments.put(key, value);
    }

    public String getArgument(ButtonInteractionEvent event, String key){
        String arguments = getArguments(event);
        log.info("Arguments: {}", arguments);
        Optional<String> searchedKeyValue = Arrays.stream(arguments.split("&"))
                .filter(arg -> arg.startsWith(key + "=")).findFirst();
        if (searchedKeyValue.isPresent()) {
            return searchedKeyValue.get().split("=")[1];
        } else {
            throw new IllegalStateException(String.format("Argument '%s' konnte nicht gefunden werden.", key));
        }
    }

    /**
     * Get the parameters between the <code>?</code> and the <code>#</code>
     * <p>
     * The ? defines the beginning of the arguments (as key-value)<br>
     * The # defines the end of the arguments and serves as an uniqueifier
     * </p>
     * @param event
     * @return
     */
    public String getArguments(ButtonInteractionEvent event){
        String id = event.getComponentId();
        String argumnents = event.getComponentId().split("\\?")[1];
        String uniqueifier = event.getComponentId().split("\\?")[1].split("#")[1];
        String onlyArgs = event.getComponentId().split("\\?")[1].split("#")[0];
        log.info("Arguments: {} | Uniqueifier: {} | onlyArgs: {}", argumnents, uniqueifier, onlyArgs);
        return event.getComponentId().split("\\?")[1].split("#")[0];
    }

    public net.dv8tion.jda.api.components.buttons.Button build(){
        String idOrUrl = getId();
        if (getStyle() == ButtonStyle.LINK && getUrl() != null) {
            idOrUrl = getUrl();
        }
        net.dv8tion.jda.api.components.buttons.Button button = net.dv8tion.jda.api.components.buttons.Button.of(getStyle(), idOrUrl, getLabel(), getEmoji());
        if (isDisabled()) {
            button.asDisabled();
        }
        return button;
    }
}
