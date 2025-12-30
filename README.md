# 全校宿舍卫生管理系统 (Dormitory Hygiene Management System)

一个基于 Spring Boot + Vue 3 的现代化宿舍卫生管理系统，集成了卫生检查、评分统计、大屏可视化展示及 AI 辅助分析等功能。

## 📸 系统预览

> 可以在 `uploads/` 目录下查看更多系统截图。

## ✨ 核心功能

*   **📊 数据大屏 (Big Screen)**: 实时展示全校卫生评分排行、趋势分析、问题分布等可视化图表。
*   **📝 卫生检查 (Inspection)**: 支持检查员在线录入检查结果，上传照片，自动计算评分。
*   **🏢 宿舍管理 (Dormitory)**: 宿舍楼栋、房间及床位的基础信息管理。
*   **👨‍🎓 学生服务 (Student)**: 学生可查看自己宿舍的卫生得分及整改建议。
*   **🤖 AI 智能分析**: 集成大模型能力，对卫生情况进行智能分析和生成整改建议。
*   **📈 统计报表**: 生成多维度的卫生检查报表，支持导出。
*   **🛡️ 权限管理**: 基于角色的访问控制 (RBAC)，区分管理员、检查员和普通学生。

## 🛠️ 技术栈

### 后端 (Backend)
*   **核心框架**: Spring Boot 2.7.18
*   **语言环境**: Java 8
*   **持久层**: MyBatis-Plus 3.5.5
*   **数据库**: MySQL 8.0+
*   **工具库**: Hutool, Lombok
*   **AI SDK**: BigModel OpenAPI

### 前端 (Frontend)
*   **框架**: Vue 3 + TypeScript
*   **构建工具**: Vite
*   **UI 组件**: Element Plus
*   **图表库**: ECharts 6.0
*   **状态管理**: Pinia
*   **路由管理**: Vue Router

## 📂 项目结构

```
.
├── deploy/             # 部署脚本与配置文件
├── dorm-frontend/      # 前端 Vue 项目源码
├── src/                # 后端 Spring Boot 源码
├── uploads/            # 系统截图与上传文件示例
├── DEPLOY_GUIDE.md     # 部署详细指南
└── pom.xml             # Maven 依赖配置
```

## 🚀 快速开始

### 1. 环境准备
*   JDK 1.8+
*   Node.js 16+
*   MySQL 8.0+

### 2. 数据库初始化
1.  创建数据库 `dorm_system` (或其他名称)。
2.  执行 `src/main/resources/db/schema.sql` 建表。
3.  执行 `src/main/resources/db/data.sql` 导入初始数据。

### 3. 后端启动
```bash
# 根目录下执行
mvn clean package -DskipTests
java -jar target/dorm-system-0.0.1-SNAPSHOT.jar
```
后端服务默认运行在 `http://localhost:8080`。

### 4. 前端启动
```bash
cd dorm-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```
前端页面默认运行在 `http://localhost:5173`。

## 📦 部署

本项目包含完整的部署脚本，支持一键部署到 Linux 服务器。
详细部署步骤请参考 [DEPLOY_GUIDE.md](./DEPLOY_GUIDE.md)。
