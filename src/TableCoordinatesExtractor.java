import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TableCoordinatesExtractor {

    public static void main(String[] args) {
        try {
            File pdfFile = new File("C:/Users/mufid/hobby/OCR/Discover-AccountActivity-20230726.pdf");

            // Load the PDF document
            PDDocument document = PDDocument.load(pdfFile);

            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                PDPage page = document.getPage(pageIndex);
                System.out.println("Page " + (pageIndex + 1));

                // Create a PDFTextStripper instance for the current page
                PDFTextStripper pdfTextStripper = new PDFTextStripper();
                pdfTextStripper.setStartPage(pageIndex + 1); // Set the start page
                pdfTextStripper.setEndPage(pageIndex + 1);   // Set the end page

                // Extract text from the page
                String pageText = pdfTextStripper.getText(document);

                // Detect and print table coordinates
                List<Rectangle2D.Float> tableCoordinates = detectTables(pageText);
                if (!tableCoordinates.isEmpty()) {
                    System.out.println("Tables Found on Page " + (pageIndex + 1));
                    for (int i = 0; i < tableCoordinates.size(); i++) {
                        Rectangle2D.Float tableBounds = tableCoordinates.get(i);
                        System.out.println("Table " + (i + 1) + " - X: " + tableBounds.getX() +
                                ", Y: " + tableBounds.getY() +
                                ", Width: " + tableBounds.getWidth() +
                                ", Height: " + tableBounds.getHeight());
                    }
                } else {
                    System.out.println("No tables found on Page " + (pageIndex + 1));
                }

                System.out.println(); // Add a separator between pages
            }

            // Close the PDF document
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Rectangle2D.Float> detectTables(String pageText) {
        List<Rectangle2D.Float> tableCoordinates = new ArrayList<>();

        // Implement your table detection logic here
        // You may use regular expressions, keyword searches, or layout analysis
        // to identify and extract table coordinates on the page
        // Add table coordinates to the list as Rectangle2D.Float objects

        // For demonstration purposes, let's add a dummy table coordinate
        // Replace this with your actual logic to detect tables
        Rectangle2D.Float dummyTable = new Rectangle2D.Float(100, 200, 300, 150);
        tableCoordinates.add(dummyTable);

        return tableCoordinates;
}
}
