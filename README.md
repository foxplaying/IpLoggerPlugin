# IpLoggerPlugin - Minecraft 服务器IP记录插件

![GitHub](https://img.shields.io/badge/Bukkit-1.13~1.21+-brightgreen) ![GitHub](https://img.shields.io/badge/License-MIT-blue)

## 简介

IpLoggerPlugin 是一个轻量级的高版本Minecraft服务器插件，用于记录和查询玩家的IP地址（IPv4/IPv6）。插件提供管理员命令来查询玩家IP信息，支持多语言界面，并可将数据存储在本地SQLite数据库中。

## 功能特性

- ✅ 记录玩家连接时的公网IP地址（IPv4/IPv6）
- ✅ 管理员命令查询玩家IP信息
- ✅ 支持点击复制IP/UUID等玩家信息
- ✅ 多语言支持（中文/英文）
- ✅ 自动检查插件更新
- ✅ 轻量级SQLite数据库存储
- ✅ 可配置的玩家加入IP显示

## 安装指南

1. 下载最新版本的 `IpLogger.jar`
2. 将jar文件放入服务器的`plugins`文件夹
3. 重启或重载服务器
4. 插件会自动生成配置文件

## 命令用法

| 命令                       | 描述                 | 权限             |
| -------------------------- | -------------------- | ---------------- |
| `/getip help`              | 显示帮助信息         | `iplogger.admin` |
| `/getip <玩家名>`          | 查询指定玩家的IP记录 | `iplogger.admin` |
| `/getip reload`            | 重载插件配置         | `iplogger.admin` |
| `/getip version`           | 检查插件版本         | `iplogger.admin` |
| `/getip language <zh\|en>` | 切换插件语言         | `iplogger.admin` |

## 权限节点

- `iplogger.admin` - 默认授予OP，允许使用所有/getip命令功能

## 配置文件

插件生成的主要配置文件：

- `config.yml` - 主配置文件
- `language_zh.yml` - 中文语言文件
- `language_en.yml` - 英文语言文件
- `ipdata.db` - SQLite数据库文件

### config.yml 示例

```yaml
# 是否开启玩家加入时记录IP日志
join-log-enabled: true

# 插件默认语言 (zh/en)
language: zh
```

## 开发者API

如果您是开发者，可以通过以下方式获取插件实例：

```java
IpLoggerPlugin plugin = IpLoggerPlugin.getInstance();
```

### 主要API方法

- `getDatabase()` - 获取数据库实例
- `getLangMessage(String key)` - 获取本地化文本
- `reloadPluginConfig()` - 重载配置
- `setLanguage(String lang)` - 设置语言 (zh/en)

## 更新日志

### v1.0
- 初始发布版本
- 实现基本IP记录和查询功能
- 添加中英文支持
- 实现自动更新检查

## 问题反馈

如果您遇到任何问题或有功能建议，请在 [GitHub Issues](https://github.com/foxplaying/IpLoggerPlugin/issues) 提交。

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。
