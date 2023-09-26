import PyPDF2
import re

class PageCoordinates:
    def __init__(self, page_num):
        self.page_num = page_num
        self.first_line_coordinates = None
        self.last_line_coordinates = None

def extract_coordinates(pdf_file_path):
    # Open the PDF file
    pdf = PyPDF2.PdfFileReader(open(pdf_file_path, 'rb'))

    # Initialize a list to store page coordinates
    page_coordinates_list = []

    # Regular expression to match lines with the format "dd/mm/yy" or "dd/mm/yyyy"
    date_pattern = r'\d{2}[\/\-.]\d{2}[\/\-.]\d{2}|\d{2}[\/\-.]\d{2}[\/\-.]\d{4}'

    # Regular expression to match lines with currency symbols "$", "€", or "£"
    currency_pattern = r'[$€£]'

    # Loop through all pages and extract text
    for page_num in range(len(pdf.pages)):
        page = pdf.pages[page_num]
        extracted_text = page.extract_text()

        # Split the extracted text into lines
        lines = extracted_text.split('\n')

        # Create an instance of PageCoordinates for the current page
        page_coord = PageCoordinates(page_num + 1)

        for index, line in enumerate(lines):
            # Check if the line matches the date pattern "dd/mm/yy" or "dd/mm/yyyy"
            if re.search(date_pattern, line.strip()):
                date_line_coordinates = index + 1

                # Store the coordinates as the first found if not already set
                if page_coord.first_line_coordinates is None:
                    page_coord.first_line_coordinates = date_line_coordinates

                # Update the last found coordinates
                page_coord.last_line_coordinates = date_line_coordinates

            # Check if the line contains currency symbols "$", "€", or "£"
            if re.search(currency_pattern, line):
                currency_line_coordinates = index + 1

                # Store the coordinates as the first found if not already set
                if page_coord.first_line_coordinates is None:
                    page_coord.first_line_coordinates = currency_line_coordinates

                # Update the last found coordinates
                page_coord.last_line_coordinates = currency_line_coordinates

        # Append the PageCoordinates instance to the list
        page_coordinates_list.append(page_coord)

    return page_coordinates_list

def main():
    # Specify the PDF file path
    pdf_file_path = r'C:/Users/mufid/hobby/OCR/Discover-AccountActivity-20230726.pdf'

    # Call the function to extract coordinates and PDF metadata
    page_coordinates = extract_coordinates(pdf_file_path)
    print(f"{page_coordinates}")
    # Print coordinates for each page
    for page_coord in page_coordinates:
        print(f"{page_coord}")
        print(f"Page {page_coord.page_num} Coordinates:")
        print("First Line:", page_coord.first_line_coordinates)
        print("Last Line:", page_coord.last_line_coordinates)
        print()

    # Print PDF metadata using the 'metadata' attribute
    pdf = PyPDF2.PdfFileReader(open(pdf_file_path, 'rb'))
    metadata = pdf.documentInfo
    print("\nPDF Metadata:")
    print("Title:", metadata.title)
    print("Author:", metadata.author)
    print("Subject:", metadata.subject)
    print("Producer:", metadata.producer)
    print("Creator:", metadata.creator)
    print("Number of Pages:", pdf.getNumPages())
    print("\nEntire Page Coordinates List:")

if __name__ == "__main__":
    main()
