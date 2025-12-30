$ErrorActionPreference = "Stop"

Write-Host "=== 远程服务器诊断工具 ===" -ForegroundColor Cyan
$ServerIP = Read-Host "请输入服务器 IP (例如 121.43.26.29)"
if ([string]::IsNullOrWhiteSpace($ServerIP)) { Write-Error "IP 不能为空" }
$ServerUser = Read-Host "请输入用户名 (默认为 root)"
if ([string]::IsNullOrWhiteSpace($ServerUser)) { $ServerUser = "root" }

$RemoteHost = "$ServerUser@$ServerIP"
$RemoteDir = "/opt/dorm-system"

$Script = @"
    echo "=================================================="
    echo ">>> 1. 检查 Java 进程"
    ps -ef | grep java | grep -v grep
    
    echo "=================================================="
    echo ">>> 2. 检查 8080 端口占用"
    netstat -ntlp | grep 8080
    
    echo "=================================================="
    echo ">>> 3. 检查应用日志 (最后 100 行)"
    if [ -f $RemoteDir/log.out ]; then
        tail -n 100 $RemoteDir/log.out
    else
        echo "日志文件不存在: $RemoteDir/log.out"
        echo "尝试查找目录内容:"
        ls -la $RemoteDir
    fi
    
    echo "=================================================="
    echo ">>> 4. 检查 Nginx 错误日志"
    if [ -f /var/log/nginx/error.log ]; then
        tail -n 20 /var/log/nginx/error.log
    fi
    
    echo "=================================================="
    echo ">>> 5. 检查内存相关错误 (OOM)"
    dmesg | grep -i "kill" | tail -n 10
"@

Write-Host "正在连接服务器进行诊断..." -ForegroundColor Yellow
ssh -t $RemoteHost "$Script"
