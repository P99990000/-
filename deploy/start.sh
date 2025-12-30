#!/bin/bash

# 确保脚本在当前目录下执行
cd "$(dirname "$0")"

# 设置 Java 路径 (如果系统环境变量已配置，可直接用 java)
JAVA_CMD=java

# JAR 包名称 (根据实际打包名称修改)
APP_NAME=dorm-system-0.0.1-SNAPSHOT.jar

# 检查 JAR 包是否存在
if [ ! -f "$APP_NAME" ]; then
    echo "Error: $APP_NAME not found!"
    exit 1
fi

# 创建上传目录
if [ ! -d "uploads" ]; then
    mkdir uploads
fi

# 启动应用
echo "Starting $APP_NAME..."
# 添加 -Djava.awt.headless=true 确保在无 GUI 环境下能生成验证码图片
nohup $JAVA_CMD -jar -Djava.awt.headless=true $APP_NAME --spring.profiles.active=prod > log.out 2>&1 &

# 检查是否启动成功
PID=$!
echo "Application started with PID $PID"
echo "Logs are being written to log.out"
