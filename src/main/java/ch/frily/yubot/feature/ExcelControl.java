package ch.frily.yubot.feature;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

public class ExcelControl {

    private static final String SHEET_NAME = "Kanäle";
    private static final String[] HEADERS = {"Sync", "Typ", "Name", "Beschreibung", "Channel-ID"};

    /**
     * Generate the Excel file for the channel list.
     * @return
     */
    public static FileUpload generateExcel() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(SHEET_NAME);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle emptyDescriptionStyle = createEmptyDescriptionStyle(workbook);

        writeHeaderRow(sheet, headerStyle);

        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);

        List<GuildChannel> channels = guild.getChannels(); // already ordered by position
        int rowIndex = 1;
        for (GuildChannel channel : channels) {
            writeChannelRow(sheet, rowIndex, channel, emptyDescriptionStyle);
            rowIndex++;
        }
        int lastDataRow = rowIndex - 1;

        addSyncDropdown(sheet, lastDataRow);
        addSyncConditionalFormatting(sheet, lastDataRow);

        // Channel-ID-Spalte ausblenden, sie wird nur intern für den Re-Import benötigt
        sheet.setColumnHidden(4, true);

        autoSizeColumns(sheet);
        sheet.createFreezePane(0, 1); // Header beim Scrollen fixieren

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);

        return FileUpload.fromData(out.toByteArray(), "channels.xlsx");
    }

    /**
     * Define the header style.
     * @param workbook
     * @return
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    /**
     * Style for the description cell when no topic is available: only visual,
     * greys the cell out. The actual "no value allowed" restriction is enforced
     * via a data validation rule (see addEmptyOnlyValidation), not via locking,
     * so no sheet protection is required.
     * @param workbook
     * @return
     */
    private static CellStyle createEmptyDescriptionStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Write header with each column name.
     * @param sheet
     * @param headerStyle
     */
    private static void writeHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Write each channel row.
     * @param sheet
     * @param rowIndex
     * @param channel GuildChannel to write
     * @param emptyDescriptionStyle style used for the description cell when no topic exists
     */
    private static void writeChannelRow(Sheet sheet, int rowIndex, GuildChannel channel,
                                        CellStyle emptyDescriptionStyle) {
        Row row = sheet.createRow(rowIndex);

        // Sync-Flag
        row.createCell(0).setCellValue("true");

        // Channel type
        row.createCell(1).setCellValue(channel.getType().name());

        // Channel name
        row.createCell(2).setCellValue(channel.getName());

        // Spalte D: Beschreibung (nur Text-/Forum-/News-/Stage-Kanäle haben ein Topic)
        String description = getTopic(channel);
        Cell descriptionCell = row.createCell(3);
        if (description != null && !description.isBlank()) {
            descriptionCell.setCellValue(description);
        } else {
            // Kein Topic verfügbar -> ausgrauen und per Validation nur leere Werte erlauben
            descriptionCell.setCellStyle(emptyDescriptionStyle);
            addEmptyOnlyValidation(sheet, rowIndex, 3);
        }

        // Spalte E (versteckt): Channel-ID für den späteren Re-Import
        row.createCell(4).setCellValue(channel.getId());
    }

    /**
     * Restricts a single cell to only accept an empty value, via a custom formula
     * data validation (ISBLANK). Used for description cells where no topic exists,
     * as an alternative to sheet protection / cell locking.
     * @param sheet
     * @param rowIndex POI row index (0-based)
     * @param colIndex POI column index (0-based)
     */
    private static void addEmptyOnlyValidation(Sheet sheet, int rowIndex, int colIndex) {
        DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);

        String columnLetter = CellReference.convertNumToColString(colIndex);
        String cellRef = columnLetter + (rowIndex + 1);
        DataValidationConstraint constraint =
                validationHelper.createCustomConstraint("ISBLANK(" + cellRef + ")");

        CellRangeAddressList addressList = new CellRangeAddressList(rowIndex, rowIndex, colIndex, colIndex);

        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Keine Beschreibung möglich",
                "Für diesen Kanal ist kein Topic vorhanden, daher kann hier kein Wert eingetragen werden.");
        sheet.addValidationData(validation);
    }

    /**
     * Creates the dropdown for the Sync-Flag, with the options true/false.
     * This flag controls whether the channel should be synced or not when uploading in back to Discord.
     */
    private static void addSyncDropdown(Sheet sheet, int lastDataRow) {
        if (lastDataRow < 1) {
            return;
        }

        DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
        DataValidationConstraint constraint =
                validationHelper.createExplicitListConstraint(new String[]{"true", "false"});

        CellRangeAddressList addressList = new CellRangeAddressList(1, lastDataRow, 0, 0);

        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Ungültiger Wert", "Bitte nur 'true' oder 'false' wählen.");
        sheet.addValidationData(validation);
    }

    /**
     * Creates the dropdown for the Sync-Flag, with the options true/false.
     * This flag controls whether the channel should be synced or not when uploading in back to Discord.
     */
    private static void addTypeDropdown(Sheet sheet, int lastDataRow) {
        if (lastDataRow < 1) {
            return;
        }

        DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
        DataValidationConstraint constraint =
                validationHelper.createExplicitListConstraint(new String[]{"TEXT", "VOICE", "STAGE", "FORUM", "NEWS", "CATEGORY"});

        CellRangeAddressList addressList = new CellRangeAddressList(1, lastDataRow, 0, 0);

        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Ungültiger Wert", "Bitte nur 'true' oder 'false' wählen.");
        sheet.addValidationData(validation);
    }

    /**
     * Adds conditional formatting to the Sync column: green when "true", red when "false".
     */
    private static void addSyncConditionalFormatting(Sheet sheet, int lastDataRow) {
        if (lastDataRow < 1) {
            return;
        }

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
        CellRangeAddress[] region = {new CellRangeAddress(1, lastDataRow, 0, 0)};

        ConditionalFormattingRule trueRule =
                sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"true\"");
        PatternFormatting truePattern = trueRule.createPatternFormatting();
        truePattern.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        truePattern.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        ConditionalFormattingRule falseRule =
                sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"false\"");
        PatternFormatting falsePattern = falseRule.createPatternFormatting();
        falsePattern.setFillBackgroundColor(IndexedColors.RED.getIndex());
        falsePattern.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        sheetCF.addConditionalFormatting(region, trueRule, falseRule);
    }

    private static void autoSizeColumns(Sheet sheet) {
        for (int col = 0; col < HEADERS.length; col++) {
            sheet.autoSizeColumn(col);
        }
        sheet.setColumnWidth(0, 10 * 256);
    }

    /**
     * Get the channel topic, if available.
     * <p></p>
     * Only Text-/Forum-/News-Channels have a topic.
     * @param channel
     * @return
     */
    private static String getTopic(GuildChannel channel) {
        if (channel instanceof TextChannel textChannel) {
            return textChannel.getTopic();
        } else if (channel instanceof ForumChannel forumChannel) {
            return forumChannel.getTopic();
        } else if (channel instanceof NewsChannel newsChannel) {
            return newsChannel.getTopic();
        }
        return null;
    }
}