import clr
clr.AddReference("Microsoft.Office.Interop.Excel")
import Microsoft.Office.Interop.Excel as Excel
excel = Excel.ApplicationClass()

wb=excel.Workbooks.Open(r'C:\work\IronPythonProject\PythonApplication1\test1.xlsx')
ws=wb.Worksheets['Sheet1']
print(ws.Name)
print(ws.Cells[3,'A'].Text)

usedRange = ws.UsedRange
print(usedRange)
#print(dir(usedRange))

print(usedRange.Rows.Count)
print(usedRange.Columns.Count)

for row in usedRange.Rows:
    for i in range(0,row.Columns.Count):
        #print(row.Cells[i,i+1].Value2)
        cell = row.Cells[1,i+1]
        #print(f'{cell.Row}:{cell.Column}:{cell.Text}') # works on IPY but not work in VS
        print('{}:{}:{}'.format(cell.Row,cell.Column,cell.Text))