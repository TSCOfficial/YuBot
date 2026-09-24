package ch.frily.yubot.feature.setting;

import javax.annotation.Nullable;

/**
 * Maps the settings to all required generic utilities
 */
public record SettingOption<T>(String label, T value, @Nullable String description){}
