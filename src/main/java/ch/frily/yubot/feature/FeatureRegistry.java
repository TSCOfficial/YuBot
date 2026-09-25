package ch.frily.yubot.feature;

import lombok.Getter;

import java.util.List;

/**
 * Registers the features
 */
public class FeatureRegistry {

    @Getter
    private List<IFeature> features;

    private static FeatureRegistry instance;
    public static FeatureRegistry getInstance() {
        if (instance == null) {
            instance = new FeatureRegistry();
        }
        return instance;
    }

    /**
     * Register any feature by creating an instance of the feature
     */
    public void register() {
        this.features = List.of();
    }
}
