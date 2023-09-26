import fitz  
import pytesseract
from PIL import Image
import re
import sys

class PageCoordinates:
    def __init__(self, page_num):
        self.page_num = page_num
        self.first_line_coordinates = None
        self.last_line_coordinates = None

def perform_ocr(image):
    return pytesseract.image_to_string(image)

def extract_coordinates_from_pdf(pdf_file_path):
    pdf_document = fitz.open(pdf_file_path)
    page_coordinates_list = []

    date_pattern = r'\d{2}[\/\-.]\d{2}[\/\-.]\d{2}|\d{2}[\/\-.]\d{2}[\/\-.]\d{4}'
    currency_pattern = r'[$€£]'

    for page_num in range(len(pdf_document)):
        page = pdf_document[page_num]
        pdf_image = page.get_pixmap()
        pdf_image = Image.frombytes("RGB", [pdf_image.width, pdf_image.height], pdf_image.samples)
        ocr_result = perform_ocr(pdf_image)
        lines = ocr_result.split('\n')

        page_coord = PageCoordinates(page_num + 1)

        for index, line in enumerate(lines):
            if re.search(date_pattern, line.strip()):
                date_line_coordinates = index

                if page_coord.first_line_coordinates is None:
                    page_coord.first_line_coordinates = date_line_coordinates

                page_coord.last_line_coordinates = date_line_coordinates

            if re.search(currency_pattern, line):
                currency_line_coordinates = index

                if page_coord.first_line_coordinates is None:
                    page_coord.first_line_coordinates = currency_line_coordinates

                page_coord.last_line_coordinates = currency_line_coordinates

        page_coordinates_list.append(page_coord)

    return page_coordinates_list

def extract_coordinates_between_lines_from_pdf_page(pdf_page, start_line, end_line):
    pdf_image = pdf_page.get_pixmap()
    pdf_image = Image.frombytes("RGB", [pdf_image.width, pdf_image.height], pdf_image.samples)
    ocr_result = perform_ocr(pdf_image)
    lines = ocr_result.split('\n')

    if len(lines) >= end_line:
        text_between_lines = "\n".join(lines[start_line:end_line])
        bbox = pdf_page.bound()
        page_width, page_height = bbox[2], bbox[3]
        x1 = bbox[0]
        y1 = bbox[1] + (start_line / len(lines)) * page_height
        x2 = bbox[2]
        y2 = bbox[1] + (end_line / len(lines)) * page_height

        return [y1, x1, y2, x2]
    else:
        print("Not enough lines in the OCR result.")
        return None

def extract_coordinates_between_lines_from_pdf(pdf_path, line_pairs_list):
    pdf_document = fitz.open(pdf_path)
    results = []

    for page_number in range(len(pdf_document)):
        if page_number >= len(line_pairs_list):
            break

        start_line, end_line = line_pairs_list[page_number]
        pdf_page = pdf_document.load_page(page_number)
        result = extract_coordinates_between_lines_from_pdf_page(pdf_page, start_line, end_line)

        if result:
            results.append(result)

    return results

def main(pdf_file_path):
    page_coordinates = extract_coordinates_from_pdf(pdf_file_path)

    for page_coord in page_coordinates:
        if page_coord.first_line_coordinates is not None and page_coord.last_line_coordinates is not None:
            page_number = page_coord.page_num
            first_line = page_coord.first_line_coordinates
            last_line = page_coord.last_line_coordinates
            coordinates = extract_coordinates_between_lines_from_pdf(pdf_file_path, [(first_line, last_line)])

            if coordinates:
                print(f"Page {page_number} coordinates: y1={coordinates[0][0]}, x1={coordinates[0][1]}, y2={coordinates[0][2]}, x2={coordinates[0][3]}")

def main():
    if len(sys.argv) != 2:
        print("Usage: python script.py <pdf_file_path>")
        sys.exit(1)

    pdf_file_path = sys.argv[1]
    page_coordinates = extract_coordinates_from_pdf(pdf_file_path)

    for page_coord in page_coordinates:
        if page_coord.first_line_coordinates is not None and page_coord.last_line_coordinates is not None:
            page_number = page_coord.page_num
            first_line = page_coord.first_line_coordinates
            last_line = page_coord.last_line_coordinates
            coordinates = extract_coordinates_between_lines_from_pdf(pdf_file_path, [(first_line, last_line)])

            if coordinates:
                print(f"Page {page_number} coordinates: y1={coordinates[0][0]}, x1={coordinates[0][1]}, y2={coordinates[0][2]}, x2={coordinates[0][3]}")

if __name__ == "__main__":
    main()
    
'''  
if __name__ == "__main__":
    pdf_file_path = 'C:/Users/mufid/hobby/OCR/Discover-AccountActivity-20230726.pdf'
    main(pdf_file_path)
'''
