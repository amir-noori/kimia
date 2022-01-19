package ir.kimia.client.util;

import ir.kimia.client.common.ApplicationContext;
import ir.kimia.client.exception.NoPrinterFoundException;
import ir.kimia.client.ui.PopupDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PrintUtil {

    public static void print(Node node) {
        try {
            Printer defaultPrinter = pickPrinterDialog();

            PrinterJob job = PrinterJob.createPrinterJob();
            JobSettings jobSettings = job.getJobSettings();
            PageLayout pageLayout = jobSettings.getPageLayout();
            job.setPrinter(defaultPrinter);
            boolean proceed = job.showPageSetupDialog(ApplicationContext.getPrimaryStage());
            if (proceed) {
                final boolean printed = job.printPage(pageLayout, node);
                if (printed) {
                    job.endJob();
                    FxUtil.info("print.success");
                } else {
                    FxUtil.error("issue.while.printing");
                }
            }
        } catch (NoPrinterFoundException e) {
            FxUtil.error("no.printer.found");
        }
    }

    public static Printer getDefaultPrinter() throws NoPrinterFoundException {

        Printer defaultPrinter = Printer.getDefaultPrinter();

        if (defaultPrinter == null) {
            final ObservableSet<Printer> allPrinters = Printer.getAllPrinters();
            if (allPrinters == null || allPrinters.size() == 0) {
                throw new NoPrinterFoundException();
            }
        }

        return defaultPrinter;
    }

    public static Printer pickPrinterDialog() throws NoPrinterFoundException {
        Printer printer = null;
        final ObservableSet<Printer> allPrinters = Printer.getAllPrinters();
        if (allPrinters != null && allPrinters.size() > 0) {
            PopupDialog dialog = new PopupDialog(500, true);
            GridPane grid = dialog.getGrid();
            ComboBox<Printer> printerCombobox = new ComboBox<>();
            printerCombobox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Printer object) {
                    if (object != null) {
                        return object.getName();
                    } else {
                        return null;
                    }
                }

                @Override
                public Printer fromString(String string) {
                    if (StringUtils.isNotEmpty(string)) {
                        for (Printer printerObject : allPrinters) {
                            if (printerObject.getName().equals(string)) {
                                return printerObject;
                            }
                        }
                    } else {
                        return null;
                    }
                    return null;
                }
            });
            grid.add(new Label(FxUtil.message("choose.printer")), 0, 0);
            grid.add(printerCombobox, 1, 0);
            printerCombobox.setItems(FXCollections.observableArrayList(allPrinters));
            printerCombobox.getSelectionModel().selectFirst();
            Optional result = dialog.showAndWait();
            if (result.isPresent()) {
                ButtonBar.ButtonData buttonData = ((ButtonType) result.get()).getButtonData();
                if (buttonData.equals(ButtonBar.ButtonData.OK_DONE)) {
                    printer = printerCombobox.getSelectionModel().getSelectedItem();
                } else if (buttonData.equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                    printer = null;
                }
            }
        } else {
            throw new NoPrinterFoundException();
        }
        return printer;
    }

    public static void exportToPdfByJasper(String fileName, HashMap<String, Object> params, String outputFileName, List<?> objects) {

        try {

            setupJasperData(params, objects);

            InputStream reportStream = PrintUtil.class.getResourceAsStream("/".concat(fileName));
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

            JRPdfExporter exporter = new JRPdfExporter();

            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFileName));

            SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
            reportConfig.setSizePageToContent(true);
            reportConfig.setForceLineBreakPolicy(false);

            SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
            exportConfig.setMetadataAuthor("XARBDAR");
            exportConfig.setEncrypted(true);
            exportConfig.setAllowedPermissionsHint("PRINTING");

            exporter.setConfiguration(reportConfig);
            exporter.setConfiguration(exportConfig);
            exporter.exportReport();
            FxUtil.info("print.success");

        } catch (JRException e) {
            FxUtil.exceptionOccurred(e);
        }

    }

    public static void printByJasper(String fileName, HashMap<String, Object> params, List<?> objects) {

        try {

            setupJasperData(params, objects);

            InputStream reportStream = PrintUtil.class.getResourceAsStream("/".concat(fileName));
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

//            Printer defaultPrinter = pickPrinterDialog();
//            PrinterJob job = PrinterJob.createPrinterJob();
//            job.setPrinter(defaultPrinter);
            boolean printSucceed = JasperPrintManager.printReport(jasperPrint, true);

            if (printSucceed) {
                FxUtil.info("print.success");
            }

        } catch (JRException e) {
            FxUtil.exceptionOccurred(e);
        }

    }

    public static void setupJasperData(HashMap<String, Object> params, List<?> objects) {
        if (params == null) {
            params = new HashMap<>();
        }

        JRDataSource dataSource = new JRBeanCollectionDataSource(objects);
        params.put("invoiceRecordsDatasource", dataSource);
    }


}
