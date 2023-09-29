## Getting Started
this is a Java-based application that enables users to view and interact with PDF documents.
It offers features such as page navigation, rectangle drawing for highlighting areas of interest, and data extraction from those areas.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.


Embeded Python:

- `coordinates_miner.py` : Called in the main Java Class, it takes pdf file path as parameter, it open the pdf perform the search for specific test pattern and return a coordinates fopr possible table area that are used in Java to suggest table location in file pages.

- `tab_extraction.py` : Called in the main Java Class, on extract button click, it takes pdf file path and tables area coordinates as parameters, it open the pdf perform table extraction based on the given area coordinates and it saves the extracted data in csv file in the project root and it gives it the same name as the source pdf file  

## Usage
### Launch the Application and Open a PDF Document:

Run the Java application by executing the DocPreview class.
The PDF documentation will be loaded from the 'pdf' folder in the project workplace.
The directory is handled within the source code and is not yet through the user interface

On launch the application excutes `coordinates_miner.py` that reads throughthe pdf contents and guesses the table area based on regular expressions
The tables to be handle exhibit significant variations in structure and content(keywords, abbreviations, and spacing..)and this makes fully automated logic challenging task.
The refinement of the logic will require a combination of image processing and meticulous character-level iteration. Fine tuning pretrained models and natural language processing are also options.
Currently the implementation relay on user to correctly define table (user input for table area can be used in future implemtation as table lableing for finetuning)

### Navigate Through Pages and Mark Areas of Interest:
Use the left and right arrow buttons (← and →) to navigate through the pages of the PDF document.
Click and drag your mouse to draw rectangles on the PDF pages to mark areas of interest.

### Extract Data:

Click the "Extract" button calls `tab_extraction.py` to extract coordinates and data from the marked areas in all pages. The data (pdf file path and coordinates) will be sent to the Python script for the final extraction processing.

### Clear Marked Areas:

Click the "Clear" button to remove the marked rectangles from the current page.

### View Extracted Data:

The extracted data will be saved to csv in the project workplace. Ensure that you have Python scripts (`tab_extraction.py` and `coordinates_miner.py`) in the same directory for data processing.

## Dependency Management
#### Java:

Apache PDFBox: For rendering PDF documents.

Google Gson: For JSON serialization and deserialization.

#### Python (external libraries):

csv: writing data to and from CSV files.

tabula: extracting tables from PDF files.

pandas: data manipulation.

json: for encoding and decoding JSON. 

pdfplumber: for working with PDF files. It provides functionality to extract text and tables from PDF documents
