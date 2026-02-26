# EasyADB

一个基于 Kotlin 和 Jetpack Compose Desktop 构建的 Android 设备管理工具，为开发者提供友好的图形界面来管理和调试 Android 设备。

## 简介

EasyADB 是一款桌面应用程序，旨在简化 Android 设备的管理和调试工作。通过直观的图形界面，开发者无需使用命令行即可完成常见的 ADB 操作，包括应用管理、文件传输、设备信息查看等功能。

## 界面预览

### 设备信息
![设备信息](img/home.png)

### 应用管理
> 应用列表

![应用管理](img/app.png)
> 进程列表

![进程管理](img/task.png)

### 文件管理
![文件管理](img/filemanager.png)

### 快捷操作
![快捷操作](img/customer.png)

### 设置页面
![设置页面](img/settings.png)

## 主要特性

### 📱 设备管理
- 自动检测并连接 Android 设备
- 实时显示设备状态和信息
- 支持多设备切换
- 设备详细信息展示（品牌、型号、Android 版本、SOC 型号等）

### 📦 应用管理
- 查看已安装应用列表
- 搜索和筛选应用
- 安装/卸载应用
- 启动/停止应用
- 复制应用包名
- 查看应用详细信息

### 📁 文件管理
- 浏览设备文件系统
- 支持拖拽上传文件
- 下载设备文件到本地
- 文件搜索功能
- 批量文件操作

### ⚡ 快捷操作
- 执行自定义 Shell 命令
- 发送 ADB 命令
- 常用操作按钮组
- 命令输入/输出界面

### ⚙️ 设置选项
- 多主题支持（系统、浅色、深色及自定义主题）
- 窗口大小管理（跟随、记忆、自定义模式）
- 终端自动关闭设置
- ADB 环境配置
- 版本信息显示

## 技术栈

- **语言**: Kotlin 2.1.0
- **UI 框架**: Jetpack Compose Desktop 1.8.0
- **设计系统**: Material Design 3
- **ADB 通信**: Android DDMLib 31.3.1
- **状态管理**: Lifecycle ViewModel Compose 2.9.2
- **数据持久化**: Multiplatform Settings 1.3.0
- **文件选择**: FileKit 0.10.0
- **JSON 处理**: Gson 2.10
- **构建工具**: Gradle with Kotlin DSL

## 系统要求

- macOS / Windows
- 已安装 ADB 工具（或使用应用内置 ADB）

## 构建项目

### 克隆仓库

```bash
git clone <repository-url>
cd EasyADB
```

### 运行开发版本

```bash
./gradlew run
```

### 构建发行版

```bash
# 构建所有平台的安装包
./gradlew packageDistributionForCurrentOS

# 构建特定平台
./gradlew packageDmg    # macOS
./gradlew packageMsi    # Windows
./gradlew packageDeb    # Linux
```

构建完成后，安装包将位于 `build/compose/binaries/main/` 目录下。

## 使用指南

### 连接设备

1. 通过 USB 连接 Android 设备到电脑
2. 在设备上启用 USB 调试模式
3. 启动 EasyADB，应用会自动检测并连接设备
4. 如有多个设备，可通过顶部下拉菜单切换

### 应用管理

1. 点击侧边栏的"应用"图标进入应用管理界面
2. 使用搜索框快速查找应用
3. 点击应用可查看详细信息
4. 右键菜单提供安装、卸载、启动等操作

### 文件传输

1. 点击侧边栏的"文件"图标进入文件管理界面
2. 浏览设备文件系统
3. 拖拽文件到窗口即可上传到设备
4. 选择文件后可下载到本地

### 自定义命令

1. 点击侧边栏的"快捷操作"图标
2. 在输入框中输入 Shell 命令或 ADB 命令
3. 点击执行按钮运行命令
4. 查看命令输出结果

## 项目结构

```
EasyADB/
├── src/main/kotlin/me/xmbest/
│   ├── Main.kt                    # 应用入口
│   ├── Config.kt                  # 全局配置和状态管理
│   ├── screen/                    # UI 界面
│   │   ├── home/                  # 设备信息界面
│   │   ├── app/                   # 应用管理界面
│   │   ├── file/                  # 文件管理界面
│   │   ├── customer/              # 快捷操作界面
│   │   └── settings/              # 设置界面
│   ├── model/                     # 数据模型
│   ├── component/                 # 可复用组件
│   ├── theme/                     # 主题定义
│   ├── util/                      # 工具函数
│   └── locale/                    # 国际化
├── ddmlib/                        # ADB 封装库
│   └── src/main/java/me/xmbest/ddmlib/
│       ├── DeviceManager.kt       # 设备连接管理
│       ├── DeviceOperate.kt       # 设备操作
│       ├── FileManager.kt         # 文件传输
│       └── CmdUtil.kt             # 命令执行
└── build.gradle.kts               # 构建配置
```

## 架构设计

项目采用 MVI (Model-View-Intent) 架构模式 + Flow 实现：

- **Model**: 不可变的 UiState 数据类，表示 UI 状态
- **View**: Compose UI 组件，根据 UiState 渲染界面
- **Intent**: 密封类定义的 UiEvent，表示用户意图和操作
- **ViewModel**: 处理 UiEvent 并更新 StateFlow<UiState>

### 核心模块

- **ddmlib**: ADB 通信封装，提供设备管理、文件操作、命令执行等功能
- **Config**: 全局配置管理，包括主题、窗口设置、设备状态等
- **Navigation**: 页面导航和路由管理
- **Theme**: Material Design 3 主题系统，支持多种配色方案

## 开发说明

### 添加新功能

1. 在 `screen/` 目录下创建新的界面模块
2. 定义 ViewModel 管理状态
3. 在 `model/Page.kt` 中添加新页面定义
4. 更新导航逻辑

### 主题定制

在 `theme/` 目录下可以添加新的配色方案，支持的主题包括：
- 系统默认
- 浅色/深色模式
- 自定义颜色主题

### 代码规范

- 使用 Kotlin 编码规范
- 遵循 Material Design 3 设计指南
- 使用 Compose 声明式 UI 编程
- 异步操作使用 Kotlin Coroutines

## 贡献

欢迎提交 Issue 和 Pull Request 来改进项目。

