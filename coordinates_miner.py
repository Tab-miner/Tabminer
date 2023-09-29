import pdfplumber
import re
import sys
import json

def extract_tables_from_pdf(pdf_path):
    price_pattern = r'(\d{1,3}(?:,\d{3})*(?:\.\d{2})?|\d{1,3}(?:\.\d{3})*(?:,\d{2})?)'
    
    date_pattern = r'\d{2}/\d{2}/\d{2}|\d{2}/\d{2}|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec\s\d{2}'
    page_coordinates_list = []
   
    with pdfplumber.open(pdf_path) as pdf:
        for page in pdf.pages:
           
            characters = page.extract_words()

            
            potential_table_lines = []
            table_coordinates = []

            current_line = ""
            current_line_coordinates = []

            for char in characters:
                current_line += char['text']
                current_line_coordinates.append((char['x0'], char['top'], char['x1'], char['bottom']))

                if re.search(date_pattern, current_line) and re.search(price_pattern, current_line) or re.search(date_pattern, current_line) and "$" in current_line:
                    potential_table_lines.append(current_line)
                    table_coordinates.extend(current_line_coordinates)

                    current_line = ""
                    current_line_coordinates = []

           
            if len(potential_table_lines) >= 3:  
                padding = 5
                if table_coordinates:
                    min_x0 = round(min(coord[0] for coord in table_coordinates)) - padding
                    min_top = round(min(coord[1] for coord in table_coordinates)) - padding
                    max_x1 = round(max(coord[2] for coord in table_coordinates)) + padding
                    max_bottom = round(max(coord[3] for coord in table_coordinates)) + padding

                    table_bbox = (min_x0, min_top, max_x1, max_bottom)

                    page_coordinates_list.append((page.page_number, table_bbox))


    return page_coordinates_list


        
def main(pdf_file_path):
    page_coordinates_list = extract_tables_from_pdf(pdf_file_path)
    coordinates_data = []
    
    for page_num, line_coordinates in page_coordinates_list:
        if line_coordinates:
            coordinates_data.append({
                'page_number': page_num,
                'line_coordinates': line_coordinates
            })

   
    coordinates_json = json.dumps(coordinates_data)
    print(coordinates_json)
    print("PROCESS COMPLETE")

    

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python script.py <pdf_file_path>")
        sys.exit(1)

    pdf_file_path = sys.argv[1]
    main(pdf_file_path)