'''

Below code works on CMD console but not work in PyCharm, need to check

ref:
https://cloud.tencent.com/developer/ask/sof/106554640

'''

import clr
# either add the location of Microsoft.Office.Interop.Excel.dll to sys.path,
# or copy Microsoft.Office.Interop.Excel to the work directory

'''
clr.AddReference('System')

from System import Reflection
import System
from System import Reflection
Reflection.Assembly.LoadFile(r'C:\work\python\Excels\Microsoft.Office.Interop.Excel.dll')
'''

import os, sys
sys.path.append(r'C:\work\python\Excels')
clr.AddReference("Microsoft.Office.Interop.Excel")

import Microsoft.Office.Interop.Excel as Excel
from Microsoft.Office.Interop.Excel import Workbooks, Sheets, Range, Worksheet

excel = Excel.Application()
wb=excel.Workbooks.Open(r'C:\work\IronPythonProject\PythonApplication1\test1.xlsx')
wb.Name
wb.Worksheets.Count
ws=wb.Worksheets['Sheet1']

wss = Sheets(wb.Sheets) # wb.Sheets woulb be generic type System.__ComObject
ws = Worksheet(wss.get_Item(1))
ws.Name
ws = Worksheet(wss.get_Item('Sheet1'))
ws.Name
cells = Range(ws.Cells)
singleCell = Range(cells.get_Item(3,5)) # even get_Item() returns System.__ComObject here
singleValue = singleCell.get_Value()

usedRange=ws.UsedRange
ws.UsedRange.Cells

usedRange.Columns.Count

usedRange.Rows.Count
Range(usedRange.get_Item(1,2)).get_Value()

for i in range(1,usedRange.Rows.Count+1):
    for j in range(1,usedRange.Columns.Count+1):
        print(Range(usedRange.get_Item(i,j)).get_Value())
