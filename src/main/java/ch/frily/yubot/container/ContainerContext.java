package ch.frily.yubot.container;

import ch.frily.yubot.interaction.ArgumentComponent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Everything a {@link StaticContainerRegistry} entry needs to build its container.
 * <p>
 * Arguments a container depends on fall into two classes, and the difference is what makes
 * pagination work for containers with arbitrary requirements:
 * <ul>
 *     <li><b>Derived from the interaction</b> ({@link #member()}, {@link #guild()}, {@link #user()}) —
 *     always available because every render starts from some event, so these never have to travel
 *     inside a component ID.</li>
 *     <li><b>Foreign data</b> ({@link #args()}) — filters, database IDs, a member other than the
 *     one clicking. These cannot be recovered from the event and are therefore carried as short
 *     key-value pairs in the component ID, which {@link PaginationContainer} copies into every
 *     navigation button it renders.</li>
 * </ul>
 * That split is what closes the render -> click -> render loop without
 * {@link ch.frily.yubot.interaction.button.btn.PaginationNavBtn} ever knowing which arguments a
 * concrete container needs.
 *
 * @param page Zero based index of the page to render
 * @param interaction The interaction this render originates from, {@code null} when there is none
 * @param args Foreign arguments that survive a navigation round-trip, never containing the
 *             reserved {@link #ARG_IDENTIFIER} and {@link #ARG_PAGE} keys
 */
public record ContainerContext(
        int page,
        @Nullable Interaction interaction,
        @NonNull Map<String, String> args
) {

    /** Component ID argument naming the {@link StaticContainerRegistry} entry to rebuild */
    public static final String ARG_IDENTIFIER = "identifier";

    /** Component ID argument holding the page to navigate to */
    public static final String ARG_PAGE = "p";

    public ContainerContext {
        args = Map.copyOf(args);
    }

    /**
     * Context for the first page of a container rendered from an interaction
     * @param interaction The interaction to derive member, guild and user from
     */
    public static ContainerContext of(Interaction interaction) {
        return of(interaction, 0);
    }

    /**
     * Context for a specific page of a container rendered from an interaction
     * @param interaction The interaction to derive member, guild and user from
     * @param page Zero-based index of the page to render
     */
    public static ContainerContext of(Interaction interaction, int page) {
        return new ContainerContext(page, interaction, Map.of());
    }

    /**
     * Context without any interaction
     */
    public static ContainerContext defaults() {
        return new ContainerContext(0, null, Map.of());
    }

    /**
     * Rebuild the context a navigation button was rendered with
     * <p>
     * The page comes from {@link #ARG_PAGE}, the foreign arguments are everything else the button
     * carries, and the interaction supplies the member.
     * @param event The button interaction to restore the context from
     */
    public static ContainerContext fromComponentId(ButtonInteractionEvent event) {
        Map<String, String> parsedArguments =
                new LinkedHashMap<>(ArgumentComponent.parseArguments(event.getComponentId()));

        int page = 0;
        String rawPage = parsedArguments.remove(ARG_PAGE);
        if (rawPage != null) {
            try {
                page = Integer.parseInt(rawPage);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        String.format("Argument '%s' ist keine gültige Seitenzahl: '%s'", ARG_PAGE, rawPage), exception);
            }
        }
        parsedArguments.remove(ARG_IDENTIFIER);

        return new ContainerContext(page, event, parsedArguments);
    }

    /**
     * The member the render is happening for
     * @return The member, or null when there is no interaction to derive it from
     */
    @Nullable
    public Member member() {
        return interaction == null ? null : interaction.getMember();
    }

    /**
     * The member the render is happening for
     * @return The member
     * @throws IllegalStateException when there is no interaction to derive it from
     */
    public Member requireMember() {
        Member member = member();
        if (member == null) {
            throw new IllegalStateException("Dieser Container benötigt ein Mitglied, es ist aber keine Interaktion vorhanden.");
        }
        return member;
    }

    @Nullable
    public Guild guild() {
        return interaction == null ? null : interaction.getGuild();
    }

    @Nullable
    public User user() {
        return interaction == null ? null : interaction.getUser();
    }

    public Optional<String> arg(String key) {
        return Optional.ofNullable(args.get(key));
    }

    /**
     * @throws IllegalStateException when the argument is missing
     */
    public String requireArg(String key) {
        return arg(key).orElseThrow(() -> new IllegalStateException(
                String.format("Argument '%s' konnte nicht gefunden werden.", key)));
    }

    public int argAsInt(String key, int fallback) {
        return arg(key).map(value -> {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }).orElse(fallback);
    }

    /**
     * Resolve an argument holding a member snowflake against the guild of the interaction
     * @param key The argument holding the member ID
     * @return The member, or null when the argument is missing or the member is not cached
     */
    @Nullable
    public Member memberArg(String key) {
        Guild guild = guild();
        if (guild == null) {
            return null;
        }
        return arg(key).map(guild::getMemberById).orElse(null);
    }

    public ContainerContext withPage(int page) {
        return new ContainerContext(page, interaction, args);
    }

    /**
     * Add a foreign argument that has to survive navigation round-trips
     * <p>
     * Keep keys and values short, the whole component ID may not exceed
     * {@value ArgumentComponent#MAX_COMPONENT_ID_LENGTH} characters.
     */
    public ContainerContext with(String key, String value) {
        if (ARG_IDENTIFIER.equals(key) || ARG_PAGE.equals(key)) {
            throw new IllegalArgumentException(String.format("Der Argument-Schlüssel '%s' ist reserviert.", key));
        }
        Map<String, String> merged = new LinkedHashMap<>(args);
        merged.put(key, value);
        return new ContainerContext(page, interaction, merged);
    }
}
