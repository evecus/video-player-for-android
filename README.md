# 映画 · YingHua Video Player

一款干净、流畅的 Android 本地 + 网络视频播放器，基于 VLC 内核，支持几乎所有视频格式。

---

## 功能

### 📁 文件管理
- 自动扫描设备中所有视频文件，按文件夹分组展示
- 视频缩略图预览（可关闭）
- 文件操作：重命名、删除、复制、移动、分享

### ▶️ 播放
- VLC 内核，支持 MKV / MP4 / AVI / FLV / TS / RMVB 等几乎全部格式
- 硬件 / 软件解码可选
- 手势控制：左侧上下调亮度，右侧上下调音量，横划调进度
- 双击左半屏 / 右半屏快退 / 快进 10 秒
- 多音频轨道、多字幕轨道切换
- 外挂字幕（SRT / ASS / SSA）
- 播放速度调节（0.5x – 3x）
- 屏幕锁定
- 连续播放

### 🌐 网络视频
- 支持 HTTP / HTTPS / RTSP / RTMP / MMS / FTP
- 历史 URL 记录

### ⚙️ 设置
- 解码方式（自动 / 硬件 / 软件）
- 默认播放方向（横屏 / 竖屏 / 跟随传感器）
- 字幕大小 / 颜色
- 是否显示缩略图
- 是否连续播放

---

## 构建

### 环境要求
- Android Studio Ladybug 2024.x 或更新版本
- JDK 17
- Android SDK 36

### 本地构建
```bash
# Debug
./gradlew assembleDebug

# Release（需要签名配置）
./gradlew assembleRelease
```

### GitHub Actions 自动发布

手动触发 Actions → **Build & Release arm64** 即可自动：

1. 编译 arm64-v8a Release APK
2. 以当前时间（`YYYY.MM.DD-HHmm`）为 Tag
3. 创建 GitHub Release 并上传 APK

#### 需要在仓库 Secrets 中配置：

| Secret | 说明 |
|---|---|
| `KEYSTORE_BASE64` | 签名 keystore 文件的 base64 编码 |
| `KEYSTORE_PASSWORD` | keystore 密码 |
| `KEY_ALIAS` | key alias |
| `KEY_PASSWORD` | key 密码 |

生成 keystore 并编码：
```bash
# 生成 keystore
keytool -genkey -v -keystore yinghua.jks \
  -alias yinghua -keyalg RSA -keysize 2048 -validity 10000

# 编码为 base64
base64 yinghua.jks | tr -d '\n'
```

---

## 技术栈

| 模块 | 技术 |
|---|---|
| UI | Jetpack Compose + Material3 |
| 导航 | Navigation Compose |
| 播放内核 | VLC libvlc-all 3.6.0 |
| 数据库 | Room |
| 设置存储 | DataStore Preferences |
| 图片/缩略图 | Coil + VideoFrameDecoder |
| 依赖注入 | Hilt |
| 异步 | Kotlin Coroutines + Flow |

---

## 系统要求

- Android 8.0（API 26）及以上
- 目标 Android 16（API 36）
- 架构：arm64-v8a
