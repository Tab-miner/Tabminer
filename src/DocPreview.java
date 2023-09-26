import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
	private static JLayeredPane layered;
	private static int currentPage = 0;
	private static PDDocument _document;
	private Point startPoint = null;
	private Point endPoint = null;

	public static void main(String[] args) {
		frame = new JFrame("PDF Previewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		File file = new File("Discover.pdf");

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
			MouseAdapter adapter = new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					docPreview.startPoint = e.getPoint();
					docPreview.endPoint = null;
				}
				@Override
				public void mouseDragged(MouseEvent e) {
					docPreview.endPoint = e.getPoint();
					rectPainter.createRectangle(docPreview.startPoint, docPreview.endPoint);
			    }
				@Override
				public void mouseReleased(MouseEvent e) {
					docPreview.endPoint = e.getPoint();
					System.out.println("Mouse released at X: " + e.getX() + ", Y: " + e.getY());
					rectPainter.createRectangle(docPreview.startPoint, docPreview.endPoint);
					if (docPreview.startPoint != null && docPreview.endPoint != null) {
						sendCoordinatesAndFileNameToPython(docPreview.startPoint, docPreview.endPoint, file.getAbsolutePath());
						sendFileNameToPythonAndGetCoordinates(file.getAbsolutePath());
					}
				}

			};
			pdfPanel.addMouseListener(adapter);
			pdfPanel.addMouseMotionListener(adapter);
			// Add the pdfPanel to the frame
			frame.add(prevButton, BorderLayout.WEST);
			frame.add(nextButton, BorderLayout.EAST);
			layered = new JLayeredPane();
			layered.setBorder(BorderFactory.createTitledBorder("Move the Mouse to Move Duke"));
			layered.setPreferredSize(new Dimension(pageWidth,pageHeight));
			frame.add(layered, BorderLayout.CENTER);
			pdfPanel.setSize(new Dimension(pageWidth,pageHeight));
			layered.add(pdfPanel, JLayeredPane.DEFAULT_LAYER);
			
			rectPainter = new RectanglePainterPanel();
			rectPainter.setBounds(0,0, pageWidth,pageHeight);
			rectPainter.setVisible(true);
			rectPainter.setOpaque(false);
			layered.add(rectPainter, JLayeredPane.MODAL_LAYER);


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
	private static List<int[]> coordinatesList = new ArrayList<>();

	private static void sendCoordinatesAndFileNameToPython(Point startPoint, Point endPoint, String pdfFileName) {
		try {
			// Create an array to store the coordinates of the drawn rectangle
			int[] coordinates = {
					startPoint.y,
					startPoint.x,
					endPoint.y,
					endPoint.x
			};

			// Add the coordinates to the list
			Gson gson = new Gson();
			coordinatesList.add(coordinates);
			String coordinatesJson = gson.toJson(coordinatesList);
			// Construct a command to run the Python script with coordinates and the PDF file name as arguments
			String[] cmd = {
					"python",
					"tab_extraction.py",
					coordinatesJson.toString(),
					pdfFileName
			};

			// Print the command
			System.out.print("Command: ");
			for (String arg : cmd) {
				System.out.print(arg + " ");
			}
			System.out.println(); // Print a newline to separate it from other output

			// Execute the Python script with the PDF file name as an argument
			Process process = Runtime.getRuntime().exec(cmd);

			// Handle process output or errors if needed
			// ...

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void sendFileNameToPythonAndGetCoordinates(String pdfFileName) {
		try {

			String[] cmd = {
					"python",
					"tab_coordinates.py",
					pdfFileName
			};

			// Print the command
			System.out.print("Command: ");
			for (String arg : cmd) {
				System.out.print(arg + " ");
			}
			System.out.println(); // Print a newline to separate it from other output

			// Execute the Python script with the PDF file name as an argument
			Process process = Runtime.getRuntime().exec(cmd);

			// Handle process output or errors if needed
			// ...
			/*  BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Script executed successfully.");

                // Parse the JSON output using Gson
                Gson gson = new Gson();
                List<CoordinateData> coordinates = gson.fromJson(outputBuilder.toString(), new TypeToken<List<CoordinateData>>() {}.getType());

                // Now you have the extracted coordinates in your Java application
                for (CoordinateData coordinate : coordinates) {
                    int pageNumber = coordinate.getPageNumber();
                    double[] coords = coordinate.getCoordinates();

                    System.out.printf("Page %d coordinates: y1=%.2f, x1=%.2f, y2=%.2f, x2=%.2f%n",
                            pageNumber, coords[0], coords[1], coords[2], coords[3]);
                }
        }*/


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}