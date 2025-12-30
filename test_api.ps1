$OutputEncoding = [System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$baseUrl = "http://localhost:8080/api"

Write-Host "1. Checking Inspection Items..."
try {
    $items = Invoke-RestMethod -Uri "$baseUrl/inspection-items" -Method Get
    if ($items.data.Count -gt 0) {
        Write-Host "   SUCCESS: Found $($items.data.Count) items." -ForegroundColor Green
    } else {
        Write-Host "   FAIL: No inspection items found." -ForegroundColor Red
    }
} catch {
    Write-Host "   FAIL: Error fetching items: $_" -ForegroundColor Red
}

Write-Host "`n2. Checking Buildings Grouped..."
try {
    $buildings = Invoke-RestMethod -Uri "$baseUrl/dormitories/buildings" -Method Get
    # Note: PowerShell object properties might not show Chinese keys easily if encoding issues exist, 
    # but we can check if data is not empty.
    if ($buildings.code -eq 200) {
        Write-Host "   SUCCESS: Buildings API returned 200." -ForegroundColor Green
        # Try to find "北院" via unicode or iteration if needed, but just checking success is good start.
    } else {
        Write-Host "   FAIL: Buildings API returned $($buildings.code)" -ForegroundColor Red
    }
} catch {
    Write-Host "   FAIL: Error fetching buildings: $_" -ForegroundColor Red
}

Write-Host "`n3. Checking specific building '北10栋'..."
try {
    # URL Encode '北10栋'
    # In PowerShell, [System.Web.HttpUtility]::UrlEncode might not be available without loading assembly.
    # Using specific hex for '北10栋': %E5%8C%9710%E6%A0%8B
    $encodedName = "%E5%8C%9710%E6%A0%8B"
    $dorms = Invoke-RestMethod -Uri "$baseUrl/dormitories/by-building/$encodedName" -Method Get
    
    if ($dorms.data.Count -gt 0) {
        Write-Host "   SUCCESS: Found $($dorms.data.Count) dorms in 北10栋." -ForegroundColor Green
        
        $target = $dorms.data | Where-Object { $_.roomNumber -eq "532" -and $_.floor -eq 5 }
        if ($target) {
            Write-Host "   SUCCESS: Found Room 532 on Floor 5. ID: $($target.id)" -ForegroundColor Green
        } else {
            Write-Host "   FAIL: Room 532 on Floor 5 NOT found." -ForegroundColor Red
        }
    } else {
        Write-Host "   FAIL: No dorms found in 北10栋." -ForegroundColor Red
    }
} catch {
    Write-Host "   FAIL: Error fetching building dorms: $_" -ForegroundColor Red
}
