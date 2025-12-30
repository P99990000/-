#!/bin/bash

# 停止脚本遇到错误立即退出
set -e

echo "=== 开始在 Alibaba Cloud Linux 3 上部署 ==="

# 1. 安装必要软件
echo "[1/5] 安装 Java 8 和 Nginx..."
yum install -y java-1.8.0-openjdk nginx

# 安全加固：安装 Firewalld (如果未安装)
if ! command -v firewall-cmd &> /dev/null; then
    echo "Installing firewalld..."
    yum install -y firewalld
    systemctl enable firewalld
    systemctl start firewalld
fi

# 安全加固：配置防火墙规则
echo "Configuring Firewall..."
# 允许 SSH (建议后续限制为特定 IP)
firewall-cmd --permanent --add-service=ssh
# 允许 HTTP/HTTPS
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https
# 移除其他不必要服务 (FTP, SMTP, POP3, IMAP)
firewall-cmd --permanent --remove-service=ftp 2>/dev/null || true
firewall-cmd --permanent --remove-service=smtp 2>/dev/null || true
firewall-cmd --permanent --remove-service=pop3 2>/dev/null || true
firewall-cmd --permanent --remove-service=imap 2>/dev/null || true
# 关闭特定高危端口
firewall-cmd --permanent --remove-port=21/tcp 2>/dev/null || true
firewall-cmd --permanent --remove-port=25/tcp 2>/dev/null || true
firewall-cmd --permanent --remove-port=110/tcp 2>/dev/null || true
firewall-cmd --permanent --remove-port=143/tcp 2>/dev/null || true
firewall-cmd --reload

# 安全加固：停止不必要的服务
echo "Stopping unnecessary services..."
systemctl stop postfix 2>/dev/null || true
systemctl disable postfix 2>/dev/null || true
systemctl stop dovecot 2>/dev/null || true
systemctl disable dovecot 2>/dev/null || true
systemctl stop vsftpd 2>/dev/null || true
systemctl disable vsftpd 2>/dev/null || true

# 2. 准备目录
echo "[2/5] 准备应用目录 /opt/dorm-system..."
mkdir -p /opt/dorm-system/dist
mkdir -p /opt/dorm-system/uploads

# 3. 移动文件 (假设文件已上传到 /tmp/dorm-deploy)
echo "[3/5] 部署文件..."
# 停止旧服务（如果存在）
if [ -f "/opt/dorm-system/stop.sh" ]; then
    bash /opt/dorm-system/stop.sh || true
fi

cp /tmp/dorm-deploy/dorm-system-*.jar /opt/dorm-system/
cp /tmp/dorm-deploy/application-prod.yml /opt/dorm-system/
cp /tmp/dorm-deploy/start.sh /opt/dorm-system/
cp /tmp/dorm-deploy/stop.sh /opt/dorm-system/
cp /tmp/dorm-deploy/nginx.conf /opt/dorm-system/

# 部署前端
rm -rf /opt/dorm-system/dist/*
cp -r /tmp/dorm-deploy/dist/* /opt/dorm-system/dist/

# 部署上传的图片 (不覆盖已存在的，防止丢失数据，这里简单处理为覆盖或保留)
# 如果需要保留服务器上的新图片，应该用 rsync，这里简化处理，直接复制本地的过去
if [ -d "/tmp/dorm-deploy/uploads" ]; then
    cp -n /tmp/dorm-deploy/uploads/* /opt/dorm-system/uploads/ 2>/dev/null || true
fi

# 设置权限
chmod +x /opt/dorm-system/start.sh
chmod +x /opt/dorm-system/stop.sh

# 4. 配置 Nginx
echo "[4/5] 配置 Nginx..."
# 备份原配置
if [ ! -f "/etc/nginx/nginx.conf.bak" ]; then
    cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak
fi

# 替换配置 (注意：这里我们直接覆盖 nginx.conf，确保 server_setup.sh 里的 nginx.conf 路径正确)
# 我们需要确保 nginx.conf 里的 root 路径是 /opt/dorm-system/dist
# 在这里动态修改一下 nginx.conf 里的 root 路径，以防万一
sed -i 's|root .*/dist;|root /opt/dorm-system/dist;|g' /opt/dorm-system/nginx.conf

cp /opt/dorm-system/nginx.conf /etc/nginx/nginx.conf

# 启动 Nginx
systemctl enable nginx
systemctl restart nginx

# 5. 启动后端
echo "[5/5] 启动后端服务..."
cd /opt/dorm-system
./start.sh

echo "=== 部署完成! ==="
echo "请访问 http://(您的服务器公网IP) 进行测试"
