# 数据库连接问题诊断与解决

## 🚨 当前错误分析

根据你提供的错误日志：
```
SQLNonTransientConnectionException: Could not create connection to database server
Caused by: java.lang.SecurityException: Permission de...
```

## 🔍 可能的解决方案

### 方案一：阿里云RDS配置检查

#### 1. 安全组配置检查
在阿里云控制台 → RDS实例 → 安全组 → 入方向规则：
- **协议类型**: TCP
- **端口范围**: 3306
- **授权对象**: 0.0.0.0/0（测试用）或你的服务器IP
- **优先级**: 100

#### 2. IP白名单检查
阿里云控制台 → RDS实例 → 白名单设置：
- 确保当前网络IP已在白名单中
- 如果没有，点击"添加白名单分组"
- 添加格式：`YOUR_IP/32` 或 `YOUR_IP`

#### 3. 获取当前IP方法
访问：https://ipinfo.io/ip 或 https://httpbin.org/ip

### 方案二：临时测试配置

如果阿里云RDS暂时无法连接，建议使用以下配置进行测试：

#### 本地MySQL测试配置
```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=secondhand_market
DB_USER=root
DB_PASSWORD=你的本地MySQL密码
FORCE_MOCK_MODE=false
```

#### 云测试数据库配置（如果需要）
```properties
# 使用免费测试数据库或你自己的测试环境
DB_HOST=your_test_host
DB_PORT=3306
DB_NAME=secondhand_market
DB_USER=test_user
DB_PASSWORD=test_password
FORCE_MOCK_MODE=false
```

### 方案三：SSL连接问题排查

如果阿里云RDS的SSL证书有问题，可以尝试：

#### 1. 检查RDS SSL状态
阿里云控制台 → RDS实例 → 数据库管理 → SSL设置

#### 2. 临时禁用SSL（仅用于测试）
在DatabaseManager.kt中临时修改连接字符串：
```kotlin
// 临时测试配置（将SSL设置改为false）
private fun getDatabaseUrlTest(): String {
    return "jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME?" +
            "useSSL=false&" +                    // 临时禁用SSL
            "serverTimezone=Asia/Shanghai&" +
            "allowPublicKeyRetrieval=true&" +
            "characterEncoding=utf8&" +
            "useUnicode=true&" +
            "connectTimeout=10000&" +           // 缩短超时时间
            "socketTimeout=30000"
}
```

## 🛠️ 立即执行的解决步骤

### 步骤1：检查阿里云RDS状态
1. 登录阿里云控制台
2. 确认RDS实例状态为"运行中"
3. 检查实例规格是否充足

### 步骤2：验证网络连接
打开命令提示符（Windows），执行：
```cmd
telnet rm-bp15zc4nz3pyyi4512o.mysql.rds.aliyuncs.com 3306
```

如果telnet不可用，使用：
```cmd
nslookup rm-bp15zc4nz3pyyi4512o.mysql.rds.aliyuncs.com
ping rm-bp15zc4nz3pyyi4512o.mysql.rds.aliyuncs.com
```

### 步骤3：临时修改配置
如果需要立即测试，可以将配置切换到：
- **本地测试**：使用localhost配置
- **临时禁用SSL**：修改DatabaseManager.kt中的连接参数

### 步骤4：查看详细日志
在Android Studio中查看更详细的错误日志：
- Logcat → 过滤器："DatabaseManager"
- 检查是否有关于SSL、认证、网络的具体错误信息

## 📞 联系支持

如果问题持续：
1. 联系阿里云技术支持
2. 提供RDS实例ID和具体错误日志
3. 请求协助检查SSL证书和连接权限

## 🔄 建议的开发流程

1. **开发阶段**：使用本地MySQL数据库
2. **测试阶段**：使用阿里云测试环境
3. **生产阶段**：使用阿里云生产环境

每个阶段配置不同的`local.properties`文件。