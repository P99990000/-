
## 系统更新指南 (Update Guide)

当您修改了代码（如本次修复手机端适配）并需要更新到阿里云服务器时，请按照以下步骤操作：

### 1. 准备更新文件 (本地已完成)
我已经为您在本地完成了最新的打包构建：
- **后端 Jar 包**: `target/dorm-system-0.0.1-SNAPSHOT.jar`
- **前端静态文件**: `dorm-frontend/dist/` 目录下的所有文件

### 2. 上传文件到服务器
使用 FTP 工具 (如 FileZilla) 或 SCP 命令将新文件上传到服务器 `/opt/dorm-system/` 目录。

**步骤:**
1.  **停止后端服务**:
    ```bash
    cd /opt/dorm-system
    ./stop.sh
    ```
2.  **上传并覆盖后端 Jar**:
    将本地的 `target/dorm-system-0.0.1-SNAPSHOT.jar` 上传覆盖服务器上的同名文件。
3.  **上传并覆盖前端文件**:
    将本地 `dorm-frontend/dist` 文件夹内的**所有内容**，上传覆盖到服务器的 `/opt/dorm-system/dist/` 目录。
    *(注意：是覆盖 dist 里面的内容，不是把 dist 文件夹本身放进去变成 dist/dist)*

### 3. 重启服务
```bash
# 启动后端
./start.sh

# 检查日志确保启动成功
tail -f log.out
```

### 4. 验证更新
- 手机访问 `http://公网IP`，检查界面是否适配。
- 电脑访问后台，功能是否正常。
