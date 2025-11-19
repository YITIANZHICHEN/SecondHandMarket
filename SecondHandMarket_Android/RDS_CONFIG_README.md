# 阿里云RDS MySQL数据库配置说明

## 配置概览

你的应用已经配置为连接阿里云RDS MySQL数据库，具体配置如下：

### 数据库信息
- **主机地址**: rm-bp15zc4nz3pyyi4512o.mysql.rds.aliyuncs.com
- **端口**: 3306
- **数据库名**: secondhand_market
- **用户名**: dbuser

### 配置文件
配置信息存储在 `local.properties` 文件中：

```properties
# 阿里云RDS MySQL数据库配置
DB_HOST=rm-bp15zc4nz3pyyi4512o.mysql.rds.aliyuncs.com
DB_PORT=3306
DB_NAME=secondhand_market
DB_USER=dbuser
DB_PASSWORD=Market123

# 设置为false启用数据库连接，true则强制使用模拟数据
FORCE_MOCK_MODE=false
```

## 阿里云RDS前置要求

### 1. 安全组配置
确保你的阿里云RDS实例安全组允许外部连接：
- **入方向**: 允许3306端口从你的应用服务器IP访问
- **白名单**: 将你的服务器IP加入RDS实例的白名单中

### 2. 数据库权限
确保用户 `dbuser` 具有以下权限：
- 对 `secondhand_market` 数据库的完整访问权限
- 可以创建、读取、更新、删除数据

### 3. SSL证书
阿里云RDS要求使用SSL连接，已在连接字符串中启用。

## 连接优化

### 数据库连接URL参数
为了更好的阿里云RDS兼容性，连接字符串包含以下优化参数：

```
useSSL=true                    // 启用SSL加密
serverTimezone=Asia/Shanghai   // 中国时区
allowPublicKeyRetrieval=true   // 允许公钥检索
characterEncoding=utf8         // UTF-8编码
useUnicode=true                // 支持Unicode
connectTimeout=30000           // 连接超时30秒
socketTimeout=60000            // Socket超时60秒
```

## 故障排除

### 1. 连接失败
如果数据库连接失败，应用会自动回退到本地模拟数据模式，查看Logcat日志：

```
Log.d(TAG, "正在连接MySQL数据库: jdbc:mysql://...")
Log.e(TAG, "MySQL数据库连接异常", e)
```

### 2. 常见错误和解决方案

#### SSL连接错误
```
Caused by: javax.net.ssl.SSLHandshakeException
```
**解决方案**: 确保RDS实例SSL配置正确，或联系阿里云技术支持

#### 连接超时
```
java.sql.SQLTimeoutException: Could not create connection
```
**解决方案**: 
- 检查安全组配置
- 确认RDS实例状态正常
- 检查网络连通性

#### 访问被拒绝
```
java.sql.SQLException: Access denied for user 'dbuser'
```
**解决方案**:
- 验证用户名和密码
- 检查用户权限
- 确认IP地址在白名单中

### 3. 调试步骤

1. **检查RDS状态**: 登录阿里云控制台确认RDS实例运行正常
2. **测试网络连通性**: 使用telnet或nc命令测试3306端口连通性
3. **验证登录信息**: 使用MySQL客户端工具测试连接
4. **查看日志**: 检查应用日志中的详细错误信息

### 4. 强制使用模拟数据
如果需要临时使用模拟数据进行开发，可以设置：

```properties
FORCE_MOCK_MODE=true
```

## 生产环境建议

1. **使用环境变量**: 不要在代码中硬编码数据库密码
2. **启用连接池**: 考虑使用HikariCP或Druid连接池
3. **监控连接**: 定期监控数据库连接状态和性能
4. **备份策略**: 制定数据库备份和恢复策略
5. **访问日志**: 开启数据库访问日志以便审计

## 安全注意事项

⚠️ **重要安全提醒**:
- 定期更改数据库密码
- 限制数据库用户的权限范围
- 监控异常访问尝试
- 使用专用网络连接（如有VPN或专线）
- 考虑启用数据库审计日志

---

如有问题，请查看应用日志或联系技术支持。