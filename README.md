## Getting Started
this is a Java-based application that enables users to view and interact with PDF documents.
It offers features such as page navigation, rectangle drawing for highlighting areas of interest, and data extraction from those areas.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.


## Usage
# Launch the Application and Open a PDF Document:

Run the Java application by executing the DocPreview class.
The PDF documentation will be loaded from the 'pdf' folder in the project workplace.
The app is in the development phase, the directory is handled within the source code and is not exposed through the user interface

# Navigate Through Pages and Mark Areas of Interest:
Use the left and right arrow buttons (← and →) to navigate through the pages of the PDF document.
Click and drag your mouse to draw rectangles on the PDF pages to mark areas of interest.

# Extract Data:

Click the "Extract" button to extract coordinates and data from the marked areas in all pages. The data and coordinates will be sent to a Python script for further processing.

# Clear Marked Areas:

Click the "Clear" button to remove the marked rectangles from the current page.

# View Extracted Data:

The extracted data will be saved to csv in the project workplace. Ensure that you have Python scripts (tab_extraction.py and tab_coordinates.py) in the same directory for data processing.

## Dependency Management
Apache PDFBox: For rendering PDF documents.
Google Gson: For JSON serialization and deserialization.