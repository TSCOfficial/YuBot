package ch.frily.yubot.feature;

/**
 * Maps the settings to all required generic utilities
 */
public record SettingOption<T>(String label, T value){}
