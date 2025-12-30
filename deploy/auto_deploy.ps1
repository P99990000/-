# 自动部署脚本
# 请在 PowerShell 中运行此脚本
# 用法: .\auto_deploy.ps1

$ErrorActionPreference = "Stop"

Write-Host "=== 全校宿舍卫生管理系统 自动部署脚本 ===" -ForegroundColor Cyan

# 1. 获取服务器信息
$ServerIP = Read-Host "请输入您的阿里云 ECS 公网 IP"
$User = "root"

if ([string]::IsNullOrWhiteSpace($ServerIP)) {
    Write-Error "IP 地址不能为空"
    exit 1
}

Write-Host "`n准备将文件部署到 root@$ServerIP ..." -ForegroundColor Yellow
Write-Host "提示: 接下来的步骤中，可能需要多次输入服务器密码。" -ForegroundColor Yellow
Write-Host "提示: 如果是第一次连接，请输入 'yes' 确认指纹。" -ForegroundColor Yellow

# 2. 检查本地文件
$ProjectRoot = ".." # 假设脚本在 deploy 目录下运行，或者调整为绝对路径
if (!(Test-Path "$PSScriptRoot\..\target\dorm-system-0.0.1-SNAPSHOT.jar")) {
    Write-Error "未找到后端 JAR 包，请先执行 mvn package"
    exit 1
}
if (!(Test-Path "$PSScriptRoot\..\dorm-frontend\dist")) {
    Write-Error "未找到前端 dist 目录，请先执行 npm run build"
    exit 1
}

# 3. 创建远程临时目录
Write-Host "`n[1/4] 清理并创建远程临时目录 /tmp/dorm-deploy ..." -ForegroundColor Cyan
ssh $User@$ServerIP "rm -rf /tmp/dorm-deploy && mkdir -p /tmp/dorm-deploy/dist && mkdir -p /tmp/dorm-deploy/uploads"

# 4. 上传文件
Write-Host "`n[2/4] 上传文件 (这可能需要几分钟) ..." -ForegroundColor Cyan

# 上传 JAR
Write-Host "上传 JAR 包..."
scp "$PSScriptRoot\..\target\dorm-system-0.0.1-SNAPSHOT.jar" "$User@$ServerIP`:/tmp/dorm-deploy/"

# 上传 配置文件和脚本
Write-Host "上传 部署脚本和配置..."
scp "$PSScriptRoot\application-prod.yml" "$User@$ServerIP`:/tmp/dorm-deploy/"
scp "$PSScriptRoot\nginx.conf" "$User@$ServerIP`:/tmp/dorm-deploy/"
scp "$PSScriptRoot\start.sh" "$User@$ServerIP`:/tmp/dorm-deploy/"
scp "$PSScriptRoot\stop.sh" "$User@$ServerIP`:/tmp/dorm-deploy/"
scp "$PSScriptRoot\server_setup.sh" "$User@$ServerIP`:/tmp/dorm-deploy/"

# 上传 前端资源
Write-Host "上传 前端资源..."
scp -r "$PSScriptRoot\..\dorm-frontend\dist\*" "$User@$ServerIP`:/tmp/dorm-deploy/dist/"

# 上传 图片资源
Write-Host "上传 图片资源..."
if (Test-Path "$PSScriptRoot\..\uploads") {
    scp -r "$PSScriptRoot\..\uploads\*" "$User@$ServerIP`:/tmp/dorm-deploy/uploads/"
}

# 5. 执行远程安装脚本
Write-Host "`n[3/4] 执行远程安装脚本..." -ForegroundColor Cyan
ssh $User@$ServerIP "chmod +x /tmp/dorm-deploy/server_setup.sh && bash /tmp/dorm-deploy/server_setup.sh"

Write-Host "`n[4/4] 部署结束！" -ForegroundColor Green
Write-Host "请访问 http://$ServerIP 验证服务是否正常。" -ForegroundColor Green
Pause
