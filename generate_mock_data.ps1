$OutputEncoding = [System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8
try {
    Write-Host "正在生成模拟数据 (覆盖过去30天，包含南北院)..."
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/mock/generate" -Method Get -TimeoutSec 300
    Write-Host "结果: $($response.message)" -ForegroundColor Green
} catch {
    Write-Host "错误: $_" -ForegroundColor Red
}
