package ch.frily.yubot.service.profile.slashcommand;

import ch.frily.yubot.interaction.command.ISlashSubcommand;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.reflections.Reflections.log;

public class ProfileSettingActivemodcolorSubcmd implements ISlashSubcommand {
    @Override
    public String getName() {
        return "activemod-color";
    }

    @Override
    public String getDescription() {
        return "Passe die Farbe des Balkens deiner ActiveMod-Statistik an.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "color", "Die Farbe des Balkens", true, true)
        );
    }

    @Override
    public Map<String, List<Command.Choice>> getAutocomplete(CommandAutoCompleteInteractionEvent event) {
        return Map.ofEntries(
                Map.entry("color", List.of(
                        new Command.Choice("Blau / Türkis", EnvKey.EMOJI_COLOR_BLAUTUERKIS.name()),
                        new Command.Choice("Blau / Petrol", EnvKey.EMOJI_COLOR_BLAUPETROL.name()),
                        new Command.Choice("Blau / Kornblume", EnvKey.EMOJI_COLOR_BLAUKORNBLUME.name()),
                        new Command.Choice("Blau / Indigo", EnvKey.EMOJI_COLOR_BLAUINDIGO.name()),
                        new Command.Choice("Blau / Eisblau", EnvKey.EMOJI_COLOR_BLAUEISBLAU.name()),
                        new Command.Choice("Blau / Blau", EnvKey.EMOJI_COLOR_BLAUBLAU.name()),
                        new Command.Choice("Blau / Azur", EnvKey.EMOJI_COLOR_BLAUAZUR.name()),
                        new Command.Choice("Blau / Aqua", EnvKey.EMOJI_COLOR_BLAUAQUA.name()),

                        new Command.Choice("Gelb / Zitrone", EnvKey.EMOJI_COLOR_GELBZITRONE.name()),
                        new Command.Choice("Gelb / Vanille", EnvKey.EMOJI_COLOR_GELBVANILLE.name()),
                        new Command.Choice("Gelb / Senf", EnvKey.EMOJI_COLOR_GELBSENF.name()),
                        new Command.Choice("Gelb / Gold", EnvKey.EMOJI_COLOR_GELBGOLD.name()),
                        new Command.Choice("Gelb / Gelb", EnvKey.EMOJI_COLOR_GELBGELB.name()),

                        new Command.Choice("Grün / Smaragd", EnvKey.EMOJI_COLOR_GRUENSMARAGD.name()),
                        new Command.Choice("Grün / Seegrün", EnvKey.EMOJI_COLOR_GRUENSEEGRUEN.name()),
                        new Command.Choice("Grün / Pistazien", EnvKey.EMOJI_COLOR_GRUENPISTAZIE.name()),
                        new Command.Choice("Grün / Olivgrün", EnvKey.EMOJI_COLOR_GRUENOLIVGRUEN.name()),
                        new Command.Choice("Grün / Minze", EnvKey.EMOJI_COLOR_GRUENMINZE.name()),
                        new Command.Choice("Grün / Lindgrün", EnvKey.EMOJI_COLOR_GRUENLINDGRUEN.name()),
                        new Command.Choice("Grün / Kleegrün", EnvKey.EMOJI_COLOR_GRUENKLEEGRUEN.name()),
                        new Command.Choice("Grün / Hellgrün", EnvKey.EMOJI_COLOR_GRUENHELLGRUEN.name()),
                        new Command.Choice("Grün / Grün", EnvKey.EMOJI_COLOR_GRUENGRUEN.name()),

                        new Command.Choice("Pink / Zartrosa", EnvKey.EMOJI_COLOR_PINKZARTROSA.name()),
                        new Command.Choice("Pink / Rosequarz", EnvKey.EMOJI_COLOR_PINKROSEQUARZ.name()),
                        new Command.Choice("Pink / Rosa", EnvKey.EMOJI_COLOR_PINKROSA.name()),
                        new Command.Choice("Pink / Pink", EnvKey.EMOJI_COLOR_PINKPINK.name()),
                        new Command.Choice("Pink / Magenta", EnvKey.EMOJI_COLOR_PINKMAGENTA.name()),
                        new Command.Choice("Pink / Fuchsia", EnvKey.EMOJI_COLOR_PINKFUCHSIA.name()),
                        new Command.Choice("Pink / Flamingo", EnvKey.EMOJI_COLOR_PINKFLAMINGO.name()),

                        new Command.Choice("Rot / Scharlach", EnvKey.EMOJI_COLOR_ROTSCHARLACH.name()),
                        new Command.Choice("Rot / Rotorange", EnvKey.EMOJI_COLOR_ROTROTORANGE.name()),
                        new Command.Choice("Rot / Rot", EnvKey.EMOJI_COLOR_ROTROT.name()),
                        new Command.Choice("Rot / Orange", EnvKey.EMOJI_COLOR_ROTORANGE.name()),
                        new Command.Choice("Rot / Mandarine", EnvKey.EMOJI_COLOR_ROTMANDARINE.name()),
                        new Command.Choice("Rot / Kupfer", EnvKey.EMOJI_COLOR_ROTKUPFER.name()),
                        new Command.Choice("Rot / Koralle", EnvKey.EMOJI_COLOR_ROTKORALLE.name()),
                        new Command.Choice("Rot / Aprikose", EnvKey.EMOJI_COLOR_ROTAPRIKOSE.name()),

                        new Command.Choice("Violett / Violett", EnvKey.EMOJI_COLOR_VIOLETTVIOLETT.name()),
                        new Command.Choice("Violett / Veilchen", EnvKey.EMOJI_COLOR_VIOLETTVEILCHEN.name()),
                        new Command.Choice("Violett / Pflaume", EnvKey.EMOJI_COLOR_VIOLETTPFLAUME.name()),
                        new Command.Choice("Violett / Orchidee", EnvKey.EMOJI_COLOR_VIOLETTORCHIDEE.name()),
                        new Command.Choice("Violett / Lila", EnvKey.EMOJI_COLOR_VIOLETTLILA.name()),
                        new Command.Choice("Violett / Lavendel", EnvKey.EMOJI_COLOR_VIOLETTLAVENDER.name()),
                        new Command.Choice("Violett / Flieder", EnvKey.EMOJI_COLOR_VIOLETTFLIEDER.name()),
                        new Command.Choice("Violett / Dunkelmagenta", EnvKey.EMOJI_COLOR_VIOLETTDUNKELMAGENTA.name()),
                        new Command.Choice("Violett / Brombeere", EnvKey.EMOJI_COLOR_VIOLETTBROMBEERE.name())
                ))
        );
    }

    @Override
    public List<Role> getAllowedRoles() {
        return ISlashSubcommand.super.getAllowedRoles();
    }


    @Override
    public void execute(@NonNull SlashCommandInteractionEvent event) throws SQLException, ClassNotFoundException {
        OptionMapping option = Objects.requireNonNull(event.getOption("color"));
        String color = option.getAsString();
        log.info("Color: {}", color);
    }
}
