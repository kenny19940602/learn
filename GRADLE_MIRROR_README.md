# Gradle 镜像配置说明

## 已配置的镜像源

### 1. Gradle Wrapper 镜像

- 阿里云镜像：`https://mirrors.aliyun.com/macports/distfiles/gradle/`
- 配置文件：`gradle/wrapper/gradle-wrapper.properties`

### 2. Maven 仓库镜像

已配置以下阿里云 Maven 仓库镜像：

- **public**: `https://maven.aliyun.com/repository/public` - 公共仓库
- **spring**: `https://maven.aliyun.com/repository/spring` - Spring 相关依赖
- **spring-plugin**: `https://maven.aliyun.com/repository/spring-plugin` - Spring 插件
- **gradle-plugin**: `https://maven.aliyun.com/repository/gradle-plugin` - Gradle 插件
- **google**: `https://maven.aliyun.com/repository/google` - Google 相关依赖
- **jcenter**: `https://maven.aliyun.com/repository/jcenter` - JCenter 仓库
- **central**: `https://maven.aliyun.com/repository/central` - Maven 中央仓库

## 配置文件说明

### gradle.properties

- 配置了网络超时时间
- 启用了并行构建和构建缓存
- 配置了 JVM 参数优化

### build.gradle

- 在 repositories 块中添加了阿里云镜像源
- 保留了 mavenCentral()作为备用

### settings.gradle

- 配置了插件仓库镜像
- 配置了依赖解析管理

## 使用方法

1. **清理缓存**（首次使用建议执行）：

   ```bash
   ./gradlew clean --refresh-dependencies
   ```

2. **正常构建**：

   ```bash
   ./gradlew build
   ```

3. **查看依赖下载情况**：
   ```bash
   ./gradlew dependencies
   ```

## 其他可用的镜像源

如果阿里云镜像仍然较慢，可以尝试以下镜像：

### 华为云镜像

```gradle
maven { url 'https://mirrors.huaweicloud.com/repository/maven/' }
```

### 腾讯云镜像

```gradle
maven { url 'https://mirrors.cloud.tencent.com/nexus/repository/maven-public/' }
```

### 网易云镜像

```gradle
maven { url 'https://mirrors.163.com/maven/repository/maven-public/' }
```

## 故障排除

1. **如果下载仍然很慢**：

   - 检查网络连接
   - 尝试使用 VPN
   - 更换其他镜像源

2. **如果出现依赖冲突**：

   - 清理 Gradle 缓存：`./gradlew clean`
   - 删除本地 Maven 仓库：`rm -rf ~/.m2/repository`

3. **如果出现 SSL 证书问题**：
   - 在 gradle.properties 中添加：`systemProp.org.gradle.internal.publish.checksums.insecure=true`
