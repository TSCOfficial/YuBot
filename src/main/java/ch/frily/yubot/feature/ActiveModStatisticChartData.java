package ch.frily.yubot.feature;

import java.util.List;

/**
 *
 * @param label
 * @param suppress grays-out the line
 * @param xData
 * @param yData
 */
public record ActiveModStatisticChartData(String label, boolean suppress, List<String> xData, List<Double> yData) {
}
