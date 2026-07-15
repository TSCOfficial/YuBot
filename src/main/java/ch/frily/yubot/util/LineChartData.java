package ch.frily.yubot.util;

import java.awt.Color;
import java.time.YearMonth;
import java.util.Date;
import java.util.List;

public record LineChartData(String label, Color colorCode, List<Date> xData, List<Double> yData) {
}
