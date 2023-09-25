import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;

public class DocPreview {
    private static JFrame frame;
    private static RectanglePainterPanel rectPainter;
    private static JPanel pdfPanel;
    private static JLabel pdfLabel;
    private static PDFRenderer pdfRenderer;
    private static int currentPage = 0;
    private static PDDocument _document;
    private Point startPoint = null;
    private Point endPoint = null;

    public static void main(String[] args) {
        frame = new JFrame("PDF Previewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        File file = new File("");

        try {
            _document = PDDocument.load(file);
            pdfRenderer = new PDFRenderer(_document);

            PDPage firstPage = _document.getPage(0);
            int pageWidth = (int) firstPage.getMediaBox().getWidth();
            int pageHeight = (int) firstPage.getMediaBox().getHeight();

            pdfPanel = new JPanel(new BorderLayout());

            pdfLabel = new JLabel();
            pdfPanel.add(pdfLabel, BorderLayout.CENTER);

            JScrollPane scrollPane = new JScrollPane(pdfPanel);
            scrollPane.setPreferredSize(new Dimension(pageWidth, pageHeight));
            frame.add(scrollPane);

            // Create navigation buttons
            JButton prevButton = new JButton("Previous");
            JButton nextButton = new JButton("Next");

            // Add action listeners to the navigation buttons
            prevButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentPage > 0) {
                        currentPage--;
                        renderAndDisplayPage(currentPage);
                    }
                }
            });

            nextButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentPage < _document.getNumberOfPages() - 1) {
                        currentPage++;
                        renderAndDisplayPage(currentPage);
                    }
                }
            });

            // Create an instance of the DocPreview class
        DocPreview docPreview = new DocPreview();

        // Add a mouse listener to the pdfPanel
        pdfPanel.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mousePressed(MouseEvent e) {
                docPreview.startPoint = e.getPoint(); // Access startPoint using the instance
                 int x = e.getX();
                int y = e.getY();
                System.out.println("Mouse pressed at X: " + x + ", Y: " + y);
                docPreview.endPoint = null;
                pdfPanel.repaint(); // Repaint the panel to show the starting point
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                docPreview.endPoint = e.getPoint(); // Access endPoint using the instance
                int x = e.getX();
                int y = e.getY();
                System.out.println("Mouse released at X: " + x + ", Y: " + y);
                if (docPreview.startPoint != null && docPreview.endPoint != null) {
                    rectPainter.createRectangle(docPreview.startPoint, new Point(x,y));
                    sendCoordinatesAndFileNameToPython(docPreview.startPoint, docPreview.endPoint, file.getAbsolutePath());
                }
            }
            
        });

            // Add the pdfPanel to the frame
            frame.add(prevButton, BorderLayout.WEST);
            frame.add(nextButton, BorderLayout.EAST);

            frame.add(pdfPanel, BorderLayout.CENTER);
            rectPainter = new RectanglePainterPanel();
            rectPainter.setBounds(0,0, 1,1);
            rectPainter.setVisible(true);
             rectPainter.setOpaque(false);
            frame.add(rectPainter, BorderLayout.PAGE_END);
            

            renderAndDisplayPage(currentPage);

            frame.pack();
            frame.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void renderAndDisplayPage(int pageNumber) {
        try {
            Image pageImage = pdfRenderer.renderImage(pageNumber, 1.0f);
            pdfLabel.setIcon(new ImageIcon(pageImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
private static void sendCoordinatesAndFileNameToPython(Point startPoint, Point endPoint, String pdfFileName) {
    try {
        // Construct a command to run the Python script with coordinates and the PDF file name as arguments
        String[] cmd = {
            "python",
            "tab_extraction.py", // Replace with your Python script filename
            Integer.toString(startPoint.y),
            Integer.toString(startPoint.x),
            Integer.toString(endPoint.y),
            Integer.toString(endPoint.x),
            pdfFileName
        };
       // Print the command
        System.out.print("Command: ");
        for (String arg : cmd) {
           System.out.print(arg + " ");
        }
        System.out.println(); // Print a newline to separate it from other output


        // Execute the Python script with coordinates and the PDF file name as arguments
        Process process = Runtime.getRuntime().exec(cmd);

        // Handle process output or errors if needed
        // ...

    } catch (IOException e) {
        e.printStackTrace();
    }
}
}