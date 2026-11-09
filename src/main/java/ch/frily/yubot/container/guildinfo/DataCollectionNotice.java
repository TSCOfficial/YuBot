package ch.frily.yubot.container.guildinfo;

import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.container.PaginationContainer;
import ch.frily.yubot.container.PaginationItem;
import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class DataCollectionNotice extends PaginationContainer {

    public DataCollectionNotice(ContainerContext context) {
        super(StaticContainerRegistry.DATA_COLLECTION_NOTICE, context);

        PaginationItem header = new PaginationItem();
        header.addChild(
                TextDisplay.of("# Datenspeicherung")
        );
        header.addChild(
                TextDisplay.of("Folgende Daten werden vom YuBot gespeichert.")
        );
        setHeader(header);

        ServiceRegistry.getInstance().getServices().forEach(service -> {
            PaginationItem item = new PaginationItem("## %s", service.getName());
            service.getRepositories().forEach(repo -> {
                item.addChild(
                        TextDisplay.ofFormat("### %s", repo.getTable())
                );

                log.info("getting enum contants for {}", repo.getTable().getTable());
                List<String> columnNames = Arrays.stream(repo.getTable().getColumnClass().getEnumConstants()).map(column -> {
                    log.info(column.toString());
                    return column.getColumn().replace("_", " ");
                }).toList();
                item.addChild(
                        TextDisplay.ofFormat("- %s", String.join("\n- ", columnNames))
                );
            });
            addItem(item);
        });
    }
}
