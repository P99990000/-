param (
    [string]$ServerIP,
    [string]$ServerUser = "root",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

# ================= 配置部分 =================
$LocalJarPath = "target\dorm-system-0.0.1-SNAPSHOT.jar"
$LocalDistPath = "dorm-frontend\dist"
$RemoteDir = "/opt/dorm-system"
$JarName = "dorm-system-0.0.1-SNAPSHOT.jar"
# ===========================================

Write-Host "`n=== 全校宿舍卫生管理系统 一键部署脚本 ===`n" -ForegroundColor Cyan

# 1. 构建项目
if (-not $SkipBuild) {
    Write-Host "[1/5] 正在构建项目..." -ForegroundColor Cyan
    
    # 后端构建
    Write-Host "正在构建后端 (Maven)..."
    # 强制设置 JAVA_HOME 到已知的工作路径
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-1.8"
    Write-Host "使用 JAVA_HOME: $env:JAVA_HOME"
    
    cmd /c "mvn clean package -DskipTests"
    if ($LASTEXITCODE -ne 0) { Write-Error "后端构建失败"; exit 1 }

    # 前端构建
    Write-Host "正在构建前端 (npm)..."
    Push-Location "dorm-frontend"
    cmd /c "npm run build"
    if ($LASTEXITCODE -ne 0) { Pop-Location; Write-Error "前端构建失败"; exit 1 }
    Pop-Location
    
    Write-Host "项目构建成功。" -ForegroundColor Green
} else {
    Write-Host "[1/5] 跳过构建，使用现有产物..." -ForegroundColor Yellow
}

# 检查产物是否存在
if (-not (Test-Path $LocalJarPath)) {
    Write-Error "错误: 找不到后端 JAR 包: $LocalJarPath"
}
if (-not (Test-Path $LocalDistPath)) {
    Write-Error "错误: 找不到前端 dist 目录: $LocalDistPath"
}

# 2. 获取服务器信息
if ([string]::IsNullOrWhiteSpace($ServerIP)) {
    $ServerIP = Read-Host "请输入阿里云服务器公网 IP"
}
if ([string]::IsNullOrWhiteSpace($ServerIP)) {
    Write-Error "IP 不能为空"
}

$RemoteHost = "$ServerUser@$ServerIP"
Write-Host "目标服务器: $RemoteHost" -ForegroundColor Gray

# 3. 打包部署文件
Write-Host "`n[2/5] 正在打包部署文件..." -ForegroundColor Cyan
$TempDir = "deploy_temp_pkg"
if (Test-Path $TempDir) { Remove-Item $TempDir -Recurse -Force }
New-Item -ItemType Directory -Path $TempDir | Out-Null

# 复制文件到临时目录
Copy-Item $LocalJarPath -Destination "$TempDir\$JarName"
New-Item -ItemType Directory -Path "$TempDir\dist" | Out-Null
Copy-Item "$LocalDistPath\*" -Destination "$TempDir\dist" -Recurse
if (Test-Path "deploy\start.sh") { Copy-Item "deploy\start.sh" -Destination $TempDir }
if (Test-Path "deploy\stop.sh") { Copy-Item "deploy\stop.sh" -Destination $TempDir }
if (Test-Path "deploy\nginx.conf") { Copy-Item "deploy\nginx.conf" -Destination $TempDir }
if (Test-Path "deploy\application-prod.yml") { Copy-Item "deploy\application-prod.yml" -Destination $TempDir }

# 压缩
$TarFile = "deploy_package.tar.gz"
tar -czf $TarFile -C $TempDir .
Remove-Item $TempDir -Recurse -Force
Write-Host "打包完成: $TarFile" -ForegroundColor Green

# 4. 上传到服务器
Write-Host "`n[3/5] 正在上传文件到服务器 (请根据提示输入密码)..." -ForegroundColor Cyan
try {
    scp $TarFile "${RemoteHost}:/tmp/"
} catch {
    Write-Error "上传失败，请检查网络或密码是否正确。"
}

# 5. 执行远程部署
Write-Host "`n[4/5] 正在服务器上执行解压和重启 (请再次输入密码)..." -ForegroundColor Cyan
$RemoteScript = @"
    # 创建目录
    mkdir -p $RemoteDir
    cd $RemoteDir
    
    # 停止旧服务
    if [ -f stop.sh ]; then
        echo "正在停止旧服务..."
        chmod +x stop.sh
        sed -i "s/\r$//" stop.sh
        ./stop.sh
    fi
    
    # 备份旧配置 (可选)
    # cp application-prod.yml application-prod.yml.bak 2>/dev/null || true
    
    # 解压新文件
    echo "正在解压..."
    tar -xzf /tmp/$TarFile -C $RemoteDir
    
    # 尝试安装字体库 (解决验证码无法生成的问题)
    echo "正在检查并安装字体库..."
    if command -v yum &> /dev/null; then
        sudo yum install -y fontconfig freetype
    elif command -v apt-get &> /dev/null; then
        sudo apt-get update
        sudo apt-get install -y fontconfig libfreetype6
    fi

    # 赋予执行权限
    chmod +x start.sh stop.sh server_setup.sh
    
    # 提示运行环境配置
    echo "提示: 如果需要应用 Nginx 安全补丁和防火墙策略，请手动运行: sudo ./server_setup.sh"
    
    # 启动服务
    echo "正在启动服务..."
    sed -i "s/\r$//" start.sh
    ./start.sh
    
    # 清理临时文件
    rm /tmp/$TarFile
    
    echo ">>> 部署操作完成！ <<<"
"@

ssh -t $RemoteHost "$RemoteScript"

# 6. 清理本地临时文件
Remove-Item $TarFile -Force
Write-Host "`n[5/5] 部署流程结束！" -ForegroundColor Green
Write-Host "请等待约 30-60 秒服务完全启动后，访问 http://$ServerIP 进行验证。" -ForegroundColor Yellow
