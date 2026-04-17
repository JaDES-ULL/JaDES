Attribute VB_Name = "M�dulo1"
Option Explicit
Private Const STR_LANGUAGE As String = "language"
Private Const STR_RESULTS As String = "results"
Private Const REF_NB_EVPI As String = "ref_NB_EVPI"
Private Const REF_NB_EVPPI As String = "ref_NB_EVPPI"
Private Const REF_NB_EVPPI_AUX As String = "ref_NB_EVPPI_aux"
Private Const COL_C_INC As Integer = 2
Private Const COL_QALE_INC As Integer = COL_C_INC + 1
Private Const COL_LE_INC As Integer = COL_C_INC + 3
Private Const NSIM As String = "nSim"
Private Const COL_DIST1 As String = "colDist1"
Private Const COL_DISTN As String = "colDistN"
Private Const REF_UMBRAL As String = "ref_umbral"
Private Const U_COSTE As String = "unitCost"
Private Const R_COSTE As String = "roundCost"
Private Const U_EFECTO As String = "unitQALE"
Private Const R_EFECTO As String = "roundQALE"
Private Const REF_EVPI As String = "ref_EVPI"
Private Const REF_ONEWAY As String = "ref_oneway"
Private Const STR_IS_PROBABILISTIC As String = "isProbabilistic"
Private Const STR_COSTS_BI As String = "costsBI"
Private Const STR_YEARS_BI As String = "yearsBI"
Private Const STR_TIME_HORIZON As String = "timeHorizon"
Private Const STR_DISCOUNT_COST As String = "discountCosts"
Private Const STR_DISCOUNT_HEALTH_OUTCOMES As String = "discountQALE"
Private Const EXTRA_COLS As Integer = 3
Private Const EXTRA_ROWS As Integer = 10
Sub OneWaySensitivity()
    Dim r As Range, i As Integer, temp As Variant
    ' Stop screen updating
    Application.ScreenUpdating = False

    shOneWay.Select
    i = 7
    Do Until shOneWay.Range("C" & i).Formula = ""
        Set r = shOneWay.Range("C" & i & ":D" & i)
        temp = ThisWorkbook.Names(r.Cells(1, 1).Value).RefersToRange.Formula
        If IsNumeric(r.Cells(1, 2)) Then
            ThisWorkbook.Names(r.Cells(1, 1).Value).RefersToRange.Formula = r.Cells(1, 2).Value
        Else
            ThisWorkbook.Names(r.Cells(1, 1).Value).RefersToRange.Formula = r.Cells(1, 2).Text
        End If
        
        shOneWay.Range("E" & i & ":O" & i).Value = shOneWay.Range("E5:O5").Value
        ThisWorkbook.Names(r.Cells(1, 1).Value).RefersToRange.Formula = temp
        i = i + 1
    Loop

    ' Order results
    shOneWay.Range(REF_ONEWAY).Select
    Range(Selection, Selection.End(xlToRight)).Select
    Range(Selection, Selection.End(xlDown)).Select
    Selection.Sort Key1:=shOneWay.Range(REF_ONEWAY).Offset(, 3), Order1:=xlDescending, Header:=xlGuess, _
        OrderCustom:=1, MatchCase:=False, Orientation:=xlTopToBottom, _
        DataOption1:=xlSortNormal

    ' TODO: Revisar para adaptarlo a la hoja
    ActiveSheet.ChartObjects("oneWayChart").Activate
    ActiveChart.Axes(xlValue).CrossesAt = shOneWay.Range("G5").Value '(shMC.Range("G10").Value - shMC.Range("J10").Value) / (shMC.Range("I10").Value - shMC.Range("L10").Value)

    ' And the other chart
    shOneWay.Range(REF_ONEWAY).Offset(0, 6).Select
    Range(Selection, Selection.End(xlToRight)).Select
    Range(Selection, Selection.End(xlDown)).Select
    Selection.Sort Key1:=shOneWay.Range(REF_ONEWAY).Offset(, 9), Order1:=xlDescending, Header:=xlGuess, _
        OrderCustom:=1, MatchCase:=False, Orientation:=xlTopToBottom, _
        DataOption1:=xlSortNormal

    ' TODO: Revisar para adaptarlo a la hoja
    ActiveSheet.ChartObjects("oneWayChart2").Activate
    ActiveChart.Axes(xlValue).CrossesAt = shOneWay.Range("I5").Value '(shMC.Range("G10").Value - shMC.Range("J10").Value) / (shMC.Range("I10").Value - shMC.Range("L10").Value)

    ' restart screen updating
    Application.ScreenUpdating = True
End Sub
'TODO: Ajustar
Sub TwoWaySensitivity()
    Dim r As Range, i As Integer, temp1 As Variant, temp2 As Variant

    ' Stop screen updating
    Application.ScreenUpdating = False
    
    shTwoWay.Select
    temp1 = ThisWorkbook.Names(shTwoWay.Range("C6").Value).RefersToRange.Formula
    temp2 = ThisWorkbook.Names(shTwoWay.Range("D6").Value).RefersToRange.Formula
    
    i = 7
    Do Until shTwoWay.Range("C" & i).Formula = ""
        Set r = shTwoWay.Range("C" & i & ":D" & i)
        ThisWorkbook.Names(shTwoWay.Range("C6").Value).RefersToRange.Formula = r.Cells(1, 1).Value
        ThisWorkbook.Names(shTwoWay.Range("D6").Value).RefersToRange.Formula = r.Cells(1, 2).Value
        
        shTwoWay.Range("E" & i & ":O" & i).Value = shTwoWay.Range("E5:O5").Value
        i = i + 1
    Loop
    ThisWorkbook.Names(shTwoWay.Range("C6").Value).RefersToRange.Formula = temp1
    ThisWorkbook.Names(shTwoWay.Range("D6").Value).RefersToRange.Formula = temp2
    ' restart screen updating
    Application.ScreenUpdating = True
End Sub
Sub TwoWaySensitivityDiscount()
    Dim r As Range, i As Integer, temp1 As Variant, temp2 As Variant

    ' Stop screen updating
    Application.ScreenUpdating = False
    
    shTwoWay2.Select
    temp1 = ThisWorkbook.Names(shTwoWay2.Range("C6").Value).RefersToRange.Formula
    temp2 = ThisWorkbook.Names(shTwoWay2.Range("D6").Value).RefersToRange.Formula
    
    i = 7
    Do Until shTwoWay2.Range("C" & i).Formula = ""
        Set r = shTwoWay2.Range("C" & i & ":D" & i)
        ThisWorkbook.Names(shTwoWay2.Range("C6").Value).RefersToRange.Formula = r.Cells(1, 1).Value
        ThisWorkbook.Names(shTwoWay2.Range("D6").Value).RefersToRange.Formula = r.Cells(1, 2).Value
        
        shTwoWay2.Range("E" & i & ":O" & i).Value = shTwoWay2.Range("E5:O5").Value
        i = i + 1
    Loop
    ThisWorkbook.Names(shTwoWay2.Range("C6").Value).RefersToRange.Formula = temp1
    ThisWorkbook.Names(shTwoWay2.Range("D6").Value).RefersToRange.Formula = temp2

    ' restart screen updating
    Application.ScreenUpdating = True
End Sub

Sub BI()
    Dim maxYears As Integer
    Dim i As Integer
    Dim oldTimeHorizon As Double
    Dim oldDiscountCosts As Double
    Dim oldDiscountHealthOutcomes As Double
    
    shBI.Select
    maxYears = Range(STR_YEARS_BI).Value
    shParameters.Select
    oldTimeHorizon = Range(STR_TIME_HORIZON).Value
    oldDiscountCosts = Range(STR_DISCOUNT_COST).Value
    oldDiscountHealthOutcomes = Range(STR_DISCOUNT_HEALTH_OUTCOMES).Value
    Range(STR_DISCOUNT_COST).Value = 0
    Range(STR_DISCOUNT_HEALTH_OUTCOMES).Value = 0
    
    For i = 1 To maxYears
        Range(STR_TIME_HORIZON).Value = i
        shBI.Range(STR_YEARS_BI).Offset(2, 2 + i).Resize(2, 1).Value = shBI.Range(STR_COSTS_BI).Value
    Next i
    ' clean the rest
    shBI.Range(STR_YEARS_BI).Offset(2, 2 + i).Resize(2, 20 - maxYears).ClearContents

    Range(STR_TIME_HORIZON).Value = oldTimeHorizon
    Range(STR_DISCOUNT_COST).Value = oldDiscountCosts
    Range(STR_DISCOUNT_HEALTH_OUTCOMES).Value = oldDiscountHealthOutcomes
    shBI.Select

End Sub

Sub mcSimul()
    Dim nSims As Integer
    Dim i As Integer
    Dim oldProbValue As Integer
    Dim cols As Integer
    
    oldProbValue = Range(STR_IS_PROBABILISTIC).Value
    Range(STR_IS_PROBABILISTIC).Value = 1
    
    nSims = Range(NSIM).Value
    cols = shMC.Range(STR_RESULTS).Columns.Count
    
    For i = 1 To nSims
        shMC.Range("B" & (i + EXTRA_ROWS + 1)).Resize(1, cols).Value = shMC.Range(STR_RESULTS).Value
        Application.StatusBar = "Ejecutando " & i & " de " & nSims & " simulaciones de MC"
    Next i
    
    Range(STR_IS_PROBABILISTIC).Value = oldProbValue
End Sub
Sub Procesar()
    
    ' Stop screen updating
    Application.ScreenUpdating = False
    
    ' Launch MC simulations
    mcSimul
    
    shControl.Select
    Application.StatusBar = "Actualizando C/E"
    Call CE
    Application.StatusBar = "Actualizando VEIP"
    shVEIP.Select
    Range(REF_EVPI).Offset(1).Select
    
    Call EVPI(Range(Selection, Selection.End(xlDown)).Rows.Count)
    
    ' Restart screen updating
    Application.ScreenUpdating = True
    Application.StatusBar = False
    shResultados.Select
End Sub
Function GetColumna(idColumna As String)
    Dim letraColumna As String
    letraColumna = shControl.Range(idColumna).Value
    GetColumna = Range(letraColumna & ":" & letraColumna).Column
End Function
Sub CE()
    ' N�mero de simulaciones
    Dim nSimulaciones As Integer
    Dim cadena As String
    
    nSimulaciones = shControl.Range(NSIM).Value
    
    shCE.Select
    ActiveSheet.ChartObjects("CEPlane").Activate
    ActiveChart.SeriesCollection("Simulaciones de MC").XValues = shMC.Range(shMC.Cells((EXTRA_ROWS + 2), COL_QALE_INC), shMC.Cells((nSimulaciones + EXTRA_ROWS + 1), COL_QALE_INC))
    ActiveChart.SeriesCollection("Simulaciones de MC").Values = shMC.Range(shMC.Cells((EXTRA_ROWS + 2), COL_C_INC), shMC.Cells((nSimulaciones + EXTRA_ROWS + 1), COL_C_INC))
    ActiveChart.SeriesCollection("Promedio").XValues = shMC.Cells(3, COL_QALE_INC)
    ActiveChart.SeriesCollection("Promedio").Values = shMC.Cells(3, COL_C_INC)
    
    ActiveSheet.ChartObjects("CEPlane2").Activate
    ActiveChart.SeriesCollection("Simulaciones de MC").XValues = shMC.Range(shMC.Cells((EXTRA_ROWS + 2), COL_LE_INC), shMC.Cells((nSimulaciones + EXTRA_ROWS + 1), COL_LE_INC))
    ActiveChart.SeriesCollection("Simulaciones de MC").Values = shMC.Range(shMC.Cells((EXTRA_ROWS + 2), COL_C_INC), shMC.Cells((nSimulaciones + EXTRA_ROWS + 1), COL_C_INC))
    ActiveChart.SeriesCollection("Promedio").XValues = shMC.Cells(3, COL_LE_INC)
    ActiveChart.SeriesCollection("Promedio").Values = shMC.Cells(3, COL_C_INC)
    
    ActiveSheet.ChartObjects("CEPlane_detail").Activate
    ActiveChart.SeriesCollection("Simulaciones de MC").XValues = shMC.Range(shMC.Cells((EXTRA_ROWS + 2), COL_QALE_INC), shMC.Cells((nSimulaciones + EXTRA_ROWS + 1), COL_QALE_INC))
    ActiveChart.SeriesCollection("Simulaciones de MC").Values = shMC.Range(shMC.Cells((EXTRA_ROWS + 2), COL_C_INC), shMC.Cells((nSimulaciones + EXTRA_ROWS + 1), COL_C_INC))
    ActiveChart.SeriesCollection("Promedio").XValues = shMC.Cells(3, COL_QALE_INC)
    ActiveChart.SeriesCollection("Promedio").Values = shMC.Cells(3, COL_C_INC)
    
    ActiveSheet.ChartObjects("CEPlane_detail2").Activate
    ActiveChart.SeriesCollection("Simulaciones de MC").XValues = shMC.Range(shMC.Cells((EXTRA_ROWS + 2), COL_LE_INC), shMC.Cells((nSimulaciones + EXTRA_ROWS + 1), COL_LE_INC))
    ActiveChart.SeriesCollection("Simulaciones de MC").Values = shMC.Range(shMC.Cells((EXTRA_ROWS + 2), COL_C_INC), shMC.Cells((nSimulaciones + EXTRA_ROWS + 1), COL_C_INC))
    ActiveChart.SeriesCollection("Promedio").XValues = shMC.Cells(3, COL_LE_INC)
    ActiveChart.SeriesCollection("Promedio").Values = shMC.Cells(3, COL_C_INC)
End Sub
Sub EVPI(nThresholds As Integer)
    ' loop counter
    Dim threshold As Long
    
    For threshold = 1 To nThresholds
        ' Set threshold
        Range(REF_UMBRAL).Value = Range(REF_EVPI).Offset(threshold)
        
        ' Copy NBs to the corresponding threshold row
        Range(REF_EVPI).Offset(threshold, 1).Resize(, 6).Value = Range(REF_NB_EVPI).Value
    Next threshold
End Sub

