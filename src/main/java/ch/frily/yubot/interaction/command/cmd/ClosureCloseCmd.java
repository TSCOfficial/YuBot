package ch.frily.yubot.interaction.command.cmd;

import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.interaction.command.ISlashSubcommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ch.frily.yubot.feature.Closure.buildContainer;

public class ClosureCloseCmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "close";
    }

    @Override
    public String getDescription() {
        return "Schliesse alle öffentlichen Channels.";
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        event.getHook().editOriginalComponents(buildContainer(List.of(), false, false)).useComponentsV2().queue(hook -> {
            Closure.getInstance().closeChannels(progressCategories ->
                    hook.editMessageComponents(buildContainer(progressCategories, false, false)).useComponentsV2().queue()
            ).thenAccept(finalCategories -> {

                hook.editMessageComponents(buildContainer(finalCategories, false, true)).useComponentsV2().queue();
            })
                    .exceptionally(throwable -> {
                        throwable.getCause().printStackTrace();
                        return null;
                    });
        });
    }

    @Override
    public List<Permission> getDefaultPermissions() {
        return ISlashSubcommand.super.getDefaultPermissions();
    }
}
