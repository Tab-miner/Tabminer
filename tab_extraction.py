import csv
import tabula
import os
import pandas as pd
import sys

def extract_pdf_to_csv(pdf_filename, csv_filename, area_parameters):
     try:
        # Check if the PDF file exists
        if not os.path.exists(pdf_filename):
            raise FileNotFoundError(f"PDF file not found: {pdf_filename}")

        rows = []  # List to store all rows from all pages

        # Iterate through each page and extract data with the corresponding area parameters
        for page_num, area in enumerate(area_parameters, start=1):
            page_data = tabula.read_pdf(pdf_filename, pages=page_num, area=area, multiple_tables=False, guess=False, pandas_options={'header': None})
            if page_data:
                # Combine the data from multiple tables on the page
                combined_data = []
                for table in page_data:
                    combined_data.extend(table.values.tolist())

                # Add the combined data to the rows list
                rows.extend(combined_data)

        with open(csv_filename, 'w', newline='', encoding='utf-8', errors='replace') as csv_file:
            csv_writer = csv.writer(csv_file)
            for row in rows:
                # Check if the row contains valid data (not empty or None)
                if any(row):
                    # Replace 'NaN' with empty string '' before writing to CSV
                    row = [cell if pd.notna(cell) else '' for cell in row]
                    csv_writer.writerow(row)

        print(f"Data has been successfully written to '{csv_filename}'")
     except FileNotFoundError as e:
        print(f"Error: {e}")
     except Exception as e:
        print(f"An error occurred: {e}")


def main():
    if len(sys.argv) != 6:
        print("Usage: python your_python_script.py start_x start_y end_x end_y pdf_filename")
        return

    start_x = int(sys.argv[1])
    start_y = int(sys.argv[2])
    end_x = int(sys.argv[3])
    end_y = int(sys.argv[4])
    pdf_filename = sys.argv[5]

    # Use the start_x, start_y, end_x, end_y values as needed in your script
    # For example, you can define an area parameter based on these coordinates
    area_parameters = [(start_x, start_y, end_x, end_y)]

    # Use pdf_filename as the PDF file name in your script
    csv_filename = "output.csv"

    extract_pdf_to_csv(pdf_filename, csv_filename, area_parameters)

if __name__ == "__main__":
    main()
