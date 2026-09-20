$ErrorActionPreference = 'Stop'

$node = 'C:\Users\acer\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe'
$workDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$definitionJson = (& $node (Join-Path $workDir 'extract_cases.mjs') | Out-String)
$definitions = $definitionJson | ConvertFrom-Json

$source = 'C:\Users\acer\Downloads\Report5.1_Unit Test.xlsx'
$targetFolder = 'C:\Users\acer\Desktop\SEP490-SU26-G89-tests'
$target = Join-Path $targetFolder 'Report5.1_Unit_Test.xlsx'
$executedDate = [datetime]'2026-09-16'

if (-not (Test-Path -LiteralPath $source)) { throw "Template not found: $source" }
if (Test-Path -LiteralPath $target) { throw "Target already exists: $target" }
New-Item -ItemType Directory -Path $targetFolder -Force | Out-Null

$excel = $null
$workbook = $null
$verificationBook = $null
try {
    $excel = New-Object -ComObject Excel.Application
    $excel.Visible = $false
    $excel.DisplayAlerts = $false
    $excel.ScreenUpdating = $false
    $excel.EnableEvents = $false
    $excel.Calculation = -4105

    $workbook = $excel.Workbooks.Open($source, 0, $false)
    $originalNames = @($workbook.Worksheets | ForEach-Object { $_.Name })

    foreach ($definition in $definitions) {
        $sheet = $workbook.Worksheets.Item([string]$definition.sheet)
        $compact = ([string]$definition.sheet -eq 'getContracts')
        $confirmRow = if ($compact) { 30 } else { 31 }
        $resultRow = if ($compact) { 38 } else { 43 }
        $passedRow = $resultRow + 1
        $dateRow = $resultRow + 2
        $defectRow = $resultRow + 3

        $sheet.Cells.Item(1,3).Value2 = [string]$definition.module
        $sheet.Cells.Item(1,12).Value2 = [string]$definition.method
        $sheet.Cells.Item(2,3).Value2 = 'Project team'
        $sheet.Cells.Item(2,12).Value2 = 'Codex / Maven'
        $sheet.Cells.Item(3,3).Value2 = [string]$definition.requirement
        $sheet.Range('B8:E42').ClearContents()
        $sheet.Range('F7:T46').ClearContents()

        $sheet.Cells.Item(8,1).Value2 = 'Condition'
        $sheet.Cells.Item(8,2).Value2 = 'Precondition'
        $sheet.Cells.Item(8,3).Value2 = 'Scenario / Input'
        $sheet.Cells.Item(9,2).Value2 = 'Setup'
        $sheet.Cells.Item(10,2).Value2 = 'Test case'
        $sheet.Cells.Item($confirmRow,1).Value2 = 'Confirm'
        $sheet.Cells.Item(31,2).Value2 = 'Return'
        $sheet.Cells.Item(31,3).Value2 = 'Expected result'
        $sheet.Cells.Item(32,2).Value2 = 'Expected'
        $sheet.Cells.Item($resultRow,1).Value2 = 'Result'
        $sheet.Cells.Item($resultRow,2).Value2 = 'Type(N : Normal, A : Abnormal, B : Boundary)'
        $sheet.Cells.Item($passedRow,2).Value2 = 'Passed/Failed'
        $sheet.Cells.Item($dateRow,2).Value2 = 'Executed Date'
        $sheet.Cells.Item($defectRow,2).Value2 = 'Defect ID'

        $sheet.Cells.Item(5,1).Formula = "=COUNTIF(F${passedRow}:T${passedRow},`"P`")"
        $sheet.Cells.Item(5,3).Formula = "=COUNTIF(F${passedRow}:T${passedRow},`"F`")"
        $sheet.Cells.Item(5,6).Formula = '=O5-A5-C5'
        $sheet.Cells.Item(5,12).Formula = "=COUNTIF(F${resultRow}:T${resultRow},`"N`")"
        $sheet.Cells.Item(5,13).Formula = "=COUNTIF(F${resultRow}:T${resultRow},`"A`")"
        $sheet.Cells.Item(5,14).Formula = "=COUNTIF(F${resultRow}:T${resultRow},`"B`")"
        $sheet.Cells.Item(5,15).Formula = '=COUNTA(F7:T7)'

        $sheet.Cells.Item(9,3).Value2 = 'JUnit 5 + Mockito; dependencies mocked'
        for ($i = 0; $i -lt $definition.cases.Count; $i++) {
            $column = 6 + $i
            $case = $definition.cases[$i]
            $sheet.Cells.Item(7,$column).Value2 = ('UTCID{0:D2}' -f ($i + 1))
            $sheet.Cells.Item(9,$column).Value2 = 'O'
            $sheet.Cells.Item(10 + $i,3).Value2 = [string]$case[0]
            $sheet.Cells.Item(10 + $i,$column).Value2 = 'O'
            $sheet.Cells.Item(32 + $i,3).Value2 = [string]$case[1]
            $sheet.Cells.Item(32 + $i,$column).Value2 = 'O'
            $sheet.Cells.Item($resultRow,$column).Value2 = [string]$case[2]
            $sheet.Cells.Item($passedRow,$column).Value2 = 'P'
            $sheet.Cells.Item($dateRow,$column).Value = $executedDate
        }

        $sheet.Range('F7:T7').Font.Name = 'Arial'
        $sheet.Range('F7:T7').Font.Size = 6
        $sheet.Range('F7:T7').Font.Bold = $true
        $sheet.Range('F7:T7').Font.Color = 16777215
        $sheet.Cells.Item(3,3).Font.Name = 'Arial'
        $sheet.Cells.Item(3,3).Font.Size = 7
        $sheet.Cells.Item(3,3).Font.Italic = $true
        $sheet.Cells.Item(3,3).WrapText = $true
        $sheet.Rows.Item(3).RowHeight = 30
        foreach ($address in @('C9:C24','C32:C42')) {
            $range = $sheet.Range($address)
            $range.Font.Name = 'Arial'
            $range.Font.Size = 7
            $range.WrapText = $true
            $range.HorizontalAlignment = -4131
            $range.VerticalAlignment = -4108
        }
        $sheet.Rows.Item('9:24').RowHeight = 36
        $sheet.Rows.Item('32:42').RowHeight = 36
        $sheet.Range("F${dateRow}:T${dateRow}").NumberFormat = 'd/m/yy'
        $sheet.Range("F${dateRow}:T${dateRow}").Font.Name = 'Arial'
        $sheet.Range("F${dateRow}:T${dateRow}").Font.Size = 6
    }

    $cover = $workbook.Worksheets.Item('Cover')
    $cover.Cells.Item(5,6).Value = $executedDate
    $cover.Cells.Item(6,6).Value2 = 4
    $cover.Cells.Item(14,1).Value = $executedDate
    $cover.Cells.Item(14,2).Value2 = 4
    $cover.Cells.Item(14,3).Value2 = 'Unit test scope'
    $cover.Cells.Item(14,4).Value2 = 'A'
    $cover.Cells.Item(14,5).Value2 = 'Add executed unit tests for 12 requested screens'
    $cover.Cells.Item(14,6).Value2 = 'Report5.1_Unit Test'

    $methodList = $workbook.Worksheets.Item('MethodList')
    $methodList.Cells.Item(6,3).Value2 = 'Java 21; Maven 3.9.2; JUnit 5; Mockito; Spring Boot 4.0.6; Windows 11; source snapshot 1164f260.'
    $methodList.Range('A9:F20').ClearContents()
    for ($i = 0; $i -lt $definitions.Count; $i++) {
        $definition = $definitions[$i]
        $row = 9 + $i
        $methodList.Cells.Item($row,1).Value2 = $i + 1
        $methodList.Cells.Item($row,2).Value2 = [string]$definition.module
        $methodList.Cells.Item($row,3).Value2 = ([string]$definition.method + '()')
        $methodList.Cells.Item($row,4).Value2 = [string]$definition.sheet
        $methodList.Cells.Item($row,5).Value2 = [string]$definition.screen
        $methodList.Cells.Item($row,6).Value2 = 'JUnit 5; Mockito mocks; authenticated context where required'
    }
    $methodList.Range('C9:D20').Font.Name = 'Arial'
    $methodList.Range('C9:D20').Font.Size = 6

    $stats = $workbook.Worksheets.Item('Statistics')
    $stats.Cells.Item(4,6).Value2 = 'Codex / Maven'
    $stats.Cells.Item(6,6).Value = $executedDate
    $stats.Cells.Item(7,2).Value2 = '80/80 tests passed on source snapshot 1164f260. Current working tree compile is blocked by unresolved merge conflicts.'
    $stats.Range('A12:I22').ClearContents()
    $groupLabels = @('User Mgmt (4)','User Profile (2)','Company Profile (2)','Dashboard + Contract (4)')
    $groups = @(
        @('getAllUsers','createUser','getUserById','updateUser'),
        @('getMyProfile','updateMyProfile'),
        @('getCompanyProfile','updateCompanyProfile'),
        @('getOverviewStatistics','getContracts','getPendingSignatureDashboard','getStatisticalReports')
    )
    $sourceCells = @('A5','C5','F5','L5','M5','N5','O5')
    for ($g = 0; $g -lt 4; $g++) {
        $row = 12 + $g
        $stats.Cells.Item($row,1).Value2 = $g + 1
        $stats.Cells.Item($row,2).Value2 = $groupLabels[$g]
        for ($c = 0; $c -lt $sourceCells.Count; $c++) {
            $refs = @($groups[$g] | ForEach-Object { "'$_'!$($sourceCells[$c])" }) -join ','
            $stats.Cells.Item($row,3 + $c).Formula = "=SUM($refs)"
        }
    }
    $stats.Cells.Item(16,2).Value2 = 'Sub total'
    for ($column = 3; $column -le 9; $column++) {
        $letter = [char](64 + $column)
        $stats.Cells.Item(16,$column).Formula = "=SUM(${letter}12:${letter}15)"
    }
    $labels = @('Test coverage','Test successful coverage','Normal case','Abnormal case','Boundary case')
    $formulas = @('=IF(I16=0,0,(C16+D16)/I16*100)','=IF(I16=0,0,C16/I16*100)','=IF(I16=0,0,F16/I16*100)','=IF(I16=0,0,G16/I16*100)','=IF(I16=0,0,H16/I16*100)')
    for ($i = 0; $i -lt 5; $i++) {
        $row = 18 + $i
        $stats.Cells.Item($row,2).Value2 = $labels[$i]
        $stats.Cells.Item($row,4).Formula = $formulas[$i]
        $stats.Cells.Item($row,5).Value2 = '%'
    }

    $excel.CalculateFullRebuild()
    $workbook.SaveAs($target, 51)
    $workbook.Close($true)
    [void][Runtime.InteropServices.Marshal]::ReleaseComObject($workbook)
    $workbook = $null

    $verificationBook = $excel.Workbooks.Open($target, 0, $true)
    $names = @($verificationBook.Worksheets | ForEach-Object { $_.Name })
    if ($names.Count -ne 44) { throw "Expected 44 worksheets, found $($names.Count)." }
    if (($names -join '|') -ne ($originalNames -join '|')) { throw 'Worksheet names or order changed.' }
    $verificationStats = $verificationBook.Worksheets.Item('Statistics')
    $excel.CalculateFullRebuild()
    $checks = [ordered]@{
        Passed = [int]$verificationStats.Cells.Item(16,3).Value2
        Failed = [int]$verificationStats.Cells.Item(16,4).Value2
        Untested = [int]$verificationStats.Cells.Item(16,5).Value2
        Normal = [int]$verificationStats.Cells.Item(16,6).Value2
        Abnormal = [int]$verificationStats.Cells.Item(16,7).Value2
        Boundary = [int]$verificationStats.Cells.Item(16,8).Value2
        Total = [int]$verificationStats.Cells.Item(16,9).Value2
    }
    if ($checks.Passed -ne 80 -or $checks.Failed -ne 0 -or $checks.Untested -ne 0 -or $checks.Normal -ne 35 -or $checks.Abnormal -ne 37 -or $checks.Boundary -ne 8 -or $checks.Total -ne 80) {
        throw "Verification totals do not match: $($checks | ConvertTo-Json -Compress)"
    }
    $verificationBook.Close($false)
    [void][Runtime.InteropServices.Marshal]::ReleaseComObject($verificationBook)
    $verificationBook = $null
    [pscustomobject]@{ Path=$target; SheetCount=$names.Count; Passed=$checks.Passed; Failed=$checks.Failed; Untested=$checks.Untested; Normal=$checks.Normal; Abnormal=$checks.Abnormal; Boundary=$checks.Boundary; Total=$checks.Total; Length=(Get-Item -LiteralPath $target).Length; SHA256=(Get-FileHash -Algorithm SHA256 -LiteralPath $target).Hash } | ConvertTo-Json -Compress
}
finally {
    if ($verificationBook) { try { $verificationBook.Close($false) } catch {}; [void][Runtime.InteropServices.Marshal]::ReleaseComObject($verificationBook) }
    if ($workbook) { try { $workbook.Close($false) } catch {}; [void][Runtime.InteropServices.Marshal]::ReleaseComObject($workbook) }
    if ($excel) { try { $excel.Quit() } catch {}; [void][Runtime.InteropServices.Marshal]::ReleaseComObject($excel) }
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}
