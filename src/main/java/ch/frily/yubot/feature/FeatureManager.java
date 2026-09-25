package ch.frily.yubot.feature;

import java.util.ArrayList;
import java.util.List;

/**
 * The feature manager connects all features to the bot
 * <p>
 *     Loaded feature will load their content to the appropriate registry
 * </p>
 */
public class FeatureManager {

    private static FeatureManager instance;
    public static FeatureManager getInstance() {
        if (instance == null) {
            instance = new FeatureManager();
        }
        return instance;
    }
}
