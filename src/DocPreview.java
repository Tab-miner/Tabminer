import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

	public static void main(String[] args) {
		frame = new JFrame("PDF Previewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		File file = new File("pdf\\CO-Statement_092023_5417.pdf");
		
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
			JButton processButton = new JButton("Extract");
			JButton clearButton = new JButton("clear");

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
			processButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								sendCoordinatesAndFileNameToPython(file.getAbsolutePath());
							}
			});

			clearButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								rectPainter.cleanRect(currentPage);
							}
			});
		

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
					System.out.println("Mouse released at X: " + e.getX() + ", Y: " + e.getY());
					rectPainter.MouseUp(e.getPoint());
				}

			};

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(prevButton);
            buttonPanel.add(nextButton);
			buttonPanel.add(processButton);
			buttonPanel.add(clearButton);

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
			
			rectPainter = new RectanglePainterPanel(_document.getNumberOfPages());
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

	private static void sendCoordinatesAndFileNameToPython(String pdfFileName) {
		try {
			List<int[]> coordinatesList = new ArrayList<>();
			Gson gson = new Gson();

			for(int i = 0; i< rectPainter.getPageCount(); i++) {
				Rectangle rect = rectPainter.getRectangle(i);
				if(rect == null){
					int[] coordinates = {};
					coordinatesList.add(coordinates);
				}else{
					int[] coordinates = {
							rect.y,
							rect.x,
							rect.y + (int)Math.round(rect.getHeight()),
							rect.x + (int)Math.round(rect.getWidth())						
					};
					coordinatesList.add(coordinates);
				}
			}
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
			try { process.waitFor(3, TimeUnit.SECONDS); } catch (InterruptedException e) {
				System.out.println("python tab_extraction.py error: " + e.getMessage());
			}
			System.out.println("python process exit code: " + process.exitValue());

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
            
            System.out.println("Python process exited with code: ");


			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}