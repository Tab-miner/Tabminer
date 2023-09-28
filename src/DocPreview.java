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
	private static JLayeredPane layered;
	private static int currentPage = 0;
	private static PDDocument _document;
	private Point startPoint = null;
	private Point endPoint = null;

	public static void main(String[] args) {
		frame = new JFrame("PDF Previewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		File file = new File("pdf\\BA-Stmt_2023-08-24.pdf");
		//C:\\Users\\mufid\\hobby\\OCR\\Discover-AccountActivity-20230726.pdf");

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
			JButton prevButton = new JButton("\u2190");
			JButton nextButton = new JButton("\u2192");

			// Add action listeners to the navigation buttons
			prevButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentPage > 0) {
						currentPage--;
						renderAndDisplayPage(currentPage);
						rectPainter.setPage(currentPage);
					}
				}
			});

			nextButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentPage < _document.getNumberOfPages() - 1) {
						currentPage++;
						renderAndDisplayPage(currentPage);
						rectPainter.setPage(currentPage);
					}
				}
			});

			// Create an instance of the DocPreview class
			DocPreview docPreview = new DocPreview();

			// Add a mouse listener to the pdfPanel
			MouseAdapter adapter = new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					rectPainter.MouseDown(e.getPoint());
				}
				@Override
				public void mouseDragged(MouseEvent e) {
					rectPainter.drag(e.getPoint());
			    }
				@Override
				public void mouseReleased(MouseEvent e) {
					docPreview.endPoint = e.getPoint();
					System.out.println("Mouse released at X: " + e.getX() + ", Y: " + e.getY());
					rectPainter.MouseUp(e.getPoint());
					if (docPreview.startPoint != null && docPreview.endPoint != null) {
						sendCoordinatesAndFileNameToPython(docPreview.startPoint, docPreview.endPoint, file.getAbsolutePath());
						
					}
				}

			};
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(prevButton);
            buttonPanel.add(nextButton);

            // Create a panel for the entire bottom section, including the buttons
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(buttonPanel, BorderLayout.NORTH);

            frame.add(bottomPanel, BorderLayout.SOUTH);
			pdfPanel.addMouseListener(adapter);
			pdfPanel.addMouseMotionListener(adapter);
		

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
            sendFileNameToPythonAndGetCoordinates(file.getAbsolutePath());

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
			
			int[] coordinates = {
					startPoint.y,
					startPoint.x,
					endPoint.y,
					endPoint.x
			};

			
			Gson gson = new Gson();
			coordinatesList.add(coordinates);
			String coordinatesJson = gson.toJson(coordinatesList);
			
			String[] cmd = {
					"python",
					"tab_extraction.py",
					coordinatesJson.toString(),
					pdfFileName
			};

			
			System.out.print("Command: ");
			for (String arg : cmd) {
				System.out.print(arg + " ");
			}
			System.out.println(); 
			
			Process process = Runtime.getRuntime().exec(cmd);

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

			
			Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if ("OCR_PROCESS_COMPLETE".equals(line.trim())) {
                    break;
                }
            }
            StringBuilder coordinatesJsonBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                coordinatesJsonBuilder.append(line);
            }
            String coordinatesJson = coordinatesJsonBuilder.toString();
            System.out.println("Coordinates JSON data:");
            System.out.println(coordinatesJson);
            //SHould exit the code here !!!!!!!
            System.out.println("Python process exited with code: ");


			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}