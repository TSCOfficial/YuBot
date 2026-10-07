package ch.frily.yubot.service.game;

import ch.frily.yubot.listeners.IListener;
import ch.frily.yubot.service.Service;

import java.util.List;

public class WordChainGameService extends Service {
    @Override
    public String getName() {
        return "Kettenbrief Minigame";
    }

    @Override
    public String getDescription() {
        return "Schreibe ein Wort um den Satz zu erweitern.";
    }

    @Override
    public List<IListener> getListeners() {
        return List.of(
                new WordListener()
        );
    }
}
