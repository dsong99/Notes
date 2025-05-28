'''
Extract the table below a header line from Document Intelligence prebuilt-layout AnalyzeResult

tested files:
1. https://raw.githubusercontent.com/Azure-Samples/cognitive-services-REST-api-samples/master/curl/form-recognizer/sample-layout.pd
2. https://www.federalreserve.gov/publications/files/2024-dfast-results-20240626.pdf
'''
import pandas as pd
import traceback
import logging
import logging
logging.basicConfig(level=logging.DEBUG,  format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)
from azure.ai.documentintelligence.models import AnalyzeResult

class LayoutExtractor():
    def __init__(self, result=None, header_text=None):
        self.result:AnalyzeResult = result
        self.header_text:str = header_text

    def find_headerline_region(self, result:AnalyzeResult, header_text):
        header_bounding_polygon = None

        for page in result.pages:
            for line in page.lines:
                if line.content == header_text:
                    header_bounding_polygon = line.polygon
                    break
            if header_bounding_polygon:
                break
        logger.info(f'header_bounding_polygon: {header_bounding_polygon}')
        return (page.page_number,header_bounding_polygon)

    adjacent_table = None
    def identify_adjacent_table(self, result:AnalyzeResult,page_number, header_bounding_polygon):
        adjacent_table = None
        if header_bounding_polygon is None:
            return None

        for table in result.tables:
            if table.bounding_regions:
                if table.bounding_regions[0].page_number<page_number:
                    continue

                table_bounding_polygon = table.bounding_regions[0].polygon
                table.bounding_regions[0].page_number
                if table_bounding_polygon[1] > header_bounding_polygon[3] :  # Table below the header
                   adjacent_table = table
                   break

        logger.info(f'table.bounding_regions {table.bounding_regions}')
        return adjacent_table

    def extract_table(self, table):

        columnHeader = [None for _ in range(table.column_count)]
        matrix = [[None for _ in range(table.column_count)] for _ in range(table.row_count-1)]
        for cell in table.cells:
            if cell.kind is not None and cell.kind == 'columnHeader':
                columnHeader[int(cell['columnIndex'])]=cell.content
            else: #assume the rest are data cells, need to be adjusted
                matrix[int(cell['rowIndex'])-1][int(cell['columnIndex'])]=cell.content

        return pd.DataFrame(columns=columnHeader, data=matrix)

    def extract_table_below_header(self, result, header_text):
        try:
            page_number, header_bounding_polygon = self.find_headerline_region(result, header_text)
            table = self.identify_adjacent_table(result, page_number, header_bounding_polygon)
            df = self.extract_table(table)
            return df
        except Exception as ex:
            traceback.print_exc()

