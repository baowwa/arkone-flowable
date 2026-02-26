# 安全组件使用说明

## 1. FormulaEngine - 公式计算引擎

### 功能概述
基于Aviator实现的安全公式计算引擎，用于动态表单中的计算字段。

### 安全特性
- ✅ 公式长度限制：最大500字符
- ✅ 执行超时：1秒
- ✅ 函数白名单：仅允许 max/min/round/abs/ceil/floor
- ✅ 禁用反射和类加载
- ✅ 循环次数限制：1000次

### 使用示例

```java
@Autowired
private FormulaEngine formulaEngine;

// 基础运算
Map<String, Object> context = new HashMap<>();
context.put("核酸浓度", 125.5);
context.put("体积", 10);

Object result = formulaEngine.evaluate("核酸浓度 * 体积 / 660", context);
// 结果: 1.902272...

// 使用白名单函数
context.put("浓度1", 100);
context.put("浓度2", 150);
context.put("浓度3", 120);

result = formulaEngine.evaluate("max(浓度1, 浓度2, 浓度3)", context);
// 结果: 150

result = formulaEngine.evaluate("round(核酸浓度 * 1.5, 2)", context);
// 结果: 188.25
```

### 支持的函数

| 函数 | 说明 | 示例 |
|------|------|------|
| max | 返回最大值 | `max(a, b, c)` |
| min | 返回最小值 | `min(a, b, c)` |
| round | 四舍五入 | `round(value, 2)` |
| abs | 绝对值 | `abs(value)` |
| ceil | 向上取整 | `ceil(value)` |
| floor | 向下取整 | `floor(value)` |

### 异常处理

```java
try {
    Object result = formulaEngine.evaluate(formula, context);
} catch (FormulaEngine.FormulaException e) {
    // 处理公式错误
    log.error("公式计算失败: {}", e.getMessage());
}
```

---

## 2. FieldEncryptor - 字段加密器

### 功能概述
基于AES/GCM实现的字段加密器，用于敏感数据的加密存储。

### 安全特性
- ✅ 算法：AES/GCM/NoPadding
- ✅ 密钥长度：256位（通过SHA-256派生）
- ✅ IV长度：12字节（随机生成）
- ✅ 认证标签：128位
- ✅ 每次加密使用不同的随机IV

### 配置

在 `application.yml` 中配置加密密钥：

```yaml
encryption:
  key: ${ENCRYPTION_KEY:your-secret-key-change-in-production}
```

**重要提示：** 生产环境必须使用环境变量设置强密钥！

### 使用示例

```java
@Autowired
private FieldEncryptor fieldEncryptor;

// 加密敏感数据
String plainText = "zhangsan@example.com";
String encrypted = fieldEncryptor.encrypt(plainText);
// 结果: Base64编码的密文，例如 "Abc123...XYZ=="

// 解密数据
String decrypted = fieldEncryptor.decrypt(encrypted);
// 结果: "zhangsan@example.com"

// 批量加密
String[] plainTexts = {"data1", "data2", "data3"};
String[] encrypted = fieldEncryptor.encryptBatch(plainTexts);

// 批量解密
String[] decrypted = fieldEncryptor.decryptBatch(encrypted);
```

### 数据格式

加密后的数据格式：
```
Base64(IV[12字节] + 密文 + 认证标签[16字节])
```

### 异常处理

```java
try {
    String encrypted = fieldEncryptor.encrypt(plainText);
    String decrypted = fieldEncryptor.decrypt(encrypted);
} catch (FieldEncryptor.EncryptionException e) {
    // 处理加密/解密错误
    log.error("加密操作失败: {}", e.getMessage());
}
```

### 数据库集成示例

```java
@Entity
@Table(name = "lims_user")
public class User {

    @Id
    private UUID id;

    private String username;

    // 敏感字段，存储加密后的数据
    @Column(name = "email_encrypted")
    private String emailEncrypted;

    @Transient
    private String email;

    // 加密邮箱
    public void encryptEmail(FieldEncryptor encryptor) throws FieldEncryptor.EncryptionException {
        if (email != null) {
            this.emailEncrypted = encryptor.encrypt(email);
        }
    }

    // 解密邮箱
    public void decryptEmail(FieldEncryptor encryptor) throws FieldEncryptor.EncryptionException {
        if (emailEncrypted != null) {
            this.email = encryptor.decrypt(emailEncrypted);
        }
    }
}
```

---

## 测试覆盖率

### FormulaEngine
- ✅ 基础算术运算
- ✅ 复杂表达式
- ✅ 白名单函数（max/min/round/abs/ceil/floor）
- ✅ 组合函数
- ✅ 中文变量名
- ✅ 安全检查（危险关键字、长度限制）
- ✅ 异常处理（空公式、无效公式、除零）
- ✅ 浮点数精度
- ✅ 大数值计算

**测试覆盖率：> 85%**

### FieldEncryptor
- ✅ 基本加密解密
- ✅ 中文字符
- ✅ 特殊字符
- ✅ 长文本
- ✅ 批量操作
- ✅ 篡改检测（GCM认证）
- ✅ 随机IV验证
- ✅ 并发安全
- ✅ 异常处理
- ✅ 性能测试

**测试覆盖率：> 90%**

---

## 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=FormulaEngineTest
mvn test -Dtest=FieldEncryptorTest

# 生成测试覆盖率报告
mvn clean test jacoco:report
```

---

## 安全建议

### FormulaEngine
1. ❌ 不要允许用户直接输入公式到生产环境
2. ✅ 在管理界面配置公式，经过审核后使用
3. ✅ 定期审计公式使用情况
4. ✅ 监控公式执行时间，防止性能问题

### FieldEncryptor
1. ✅ 生产环境必须使用强密钥（至少32字符）
2. ✅ 密钥通过环境变量或密钥管理服务注入
3. ❌ 不要在代码中硬编码密钥
4. ✅ 定期轮换加密密钥
5. ✅ 备份时确保密钥安全存储
6. ✅ 使用HTTPS传输加密数据

---

## 性能指标

### FormulaEngine
- 简单公式（< 50字符）：< 10ms
- 复杂公式（100-500字符）：< 50ms
- 超时限制：1000ms

### FieldEncryptor
- 短文本加密（< 100字符）：< 5ms
- 长文本加密（> 10KB）：< 50ms
- 批量操作（100条）：< 500ms

---

## 依赖版本

```xml
<!-- Aviator 公式引擎 -->
<dependency>
    <groupId>com.googlecode.aviator</groupId>
    <artifactId>aviator</artifactId>
    <version>5.4.1</version>
</dependency>

<!-- JDK内置加密库 -->
<!-- javax.crypto.Cipher -->
<!-- java.security.SecureRandom -->
```

---

## 常见问题

### Q: FormulaEngine支持自定义函数吗？
A: 出于安全考虑，仅支持白名单中的6个函数。如需添加新函数，需要修改源码并经过安全审计。

### Q: FieldEncryptor可以用于文件加密吗？
A: 当前实现针对字符串字段优化，不建议用于大文件加密。大文件建议使用流式加密。

### Q: 加密后的数据可以搜索吗？
A: GCM模式的密文无法直接搜索。如需搜索，考虑使用确定性加密或搜索索引。

### Q: 如何迁移已加密的数据？
A: 需要先用旧密钥解密，再用新密钥加密。建议实现密钥版本管理。

---

## 技术支持

如有问题，请联系：
- 技术架构团队
- Email: tech@arkone.com
- 文档版本: v1.0
- 更新日期: 2026-02-25
