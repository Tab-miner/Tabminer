import csv
import tabula
import os
import pandas as pd
import sys
import json


def extract_pdf_to_csv(pdf_filename, area_parameters):
     try:
        if not os.path.exists(pdf_filename):
            raise FileNotFoundError(f"PDF file not found: {pdf_filename}")
        
        pdf_base_name = os.path.splitext(os.path.basename(pdf_filename))[0]

        rows = []  
        # Iterate through each page and extract data with the corresponding area parameters
        for page_num, area in enumerate(area_parameters, start=1):
            if len(area) == 0:
                continue
            page_data = tabula.read_pdf(pdf_filename, pages=page_num, area=area, multiple_tables=False, guess=False, pandas_options={'header': None})
            if page_data:
               
                combined_data = []
                for table in page_data:
                    combined_data.extend(table.values.tolist())

                
                rows.extend(combined_data)
                
        csv_filename = f"{pdf_base_name}.csv"

        with open(csv_filename, 'w', newline='', encoding='utf-8', errors='replace') as csv_file:
            csv_writer = csv.writer(csv_file)
            for row in rows:
                
                if any(row):
            
                    row = [cell if pd.notna(cell) else '' for cell in row]
                    csv_writer.writerow(row)

        print(f"Data has been successfully written to '{csv_filename}'")
     except FileNotFoundError as e:
        print(f"Error: {e}")
     except Exception as e:
        print(f"An error occurred: {e}")


def main():
    if len(sys.argv) != 3:
        print("Usage: python python_script.py start_x start_y end_x end_y pdf_filename")
        return


    coordinatesJson = sys.argv[1]
    pdf_filename = sys.argv[2]
    
    area_parameters =  json.loads(coordinatesJson)

    extract_pdf_to_csv(pdf_filename, area_parameters)


if __name__ == "__main__":
    main()
