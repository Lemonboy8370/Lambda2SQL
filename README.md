# Lambda2SQL

一个用于将 MyBatis-Plus `LambdaQueryWrapper` 条件代码转换为 SQL 的桌面工具。

Lambda2SQL 适合在排查接口问题、确认查询条件、编写文档、代码评审或和同事沟通 SQL 逻辑时使用。你可以粘贴简单的链式调用，也可以粘贴一小段包含 `queryWrapper` 的 Java 代码，工具会从中提取支持的 wrapper 条件并生成可读 SQL。

## 下载

请前往 GitHub Releases 下载对应系统版本：

- macOS Apple Silicon：下载 `Lambda2SQL-1.0.0.dmg`
- Windows x64：下载 Windows 安装包
- 通用 Java 版本：下载 `lambda2sql.jar` 后执行 `java -jar lambda2sql.jar`

## 功能特性

- 支持 MySQL / PostgreSQL 方言切换。
- 支持从完整 Java 代码片段中提取 `LambdaQueryWrapper` 条件。
- 支持连续链式调用，例如 `.eq(...).gt(...).limit(...)`。
- 支持 `and(w -> ...)`、`.or()` 这类嵌套条件分组。
- 支持 Java getter 引用转数据库列名，例如 `User::getCreateTime` -> `create_time`。
- 支持字符串、数字、`null` 的基础 SQL 值格式化。
- 桌面界面和异常提示均为中文。
- 支持打包为 macOS / Windows 桌面程序。

## 支持语法

| Lambda 写法 | SQL 含义 |
| --- | --- |
| `.eq(X, val)` | `col = val` |
| `.ne(X, val)` | `col != val` |
| `.gt(X, val)` | `col > val` |
| `.ge(X, val)` | `col >= val` |
| `.lt(X, val)` | `col < val` |
| `.le(X, val)` | `col <= val` |
| `.like(X, val)` | `col LIKE '%val%'` |
| `.notLike(X, val)` | `col NOT LIKE '%val%'` |
| `.likeLeft(X, val)` | `col LIKE '%val'` |
| `.likeRight(X, val)` | `col LIKE 'val%'` |
| `.isNull(X)` | `col IS NULL` |
| `.isNotNull(X)` | `col IS NOT NULL` |
| `.in(X, a, b)` | `col IN (a, b)` |
| `.notIn(X, a, b)` | `col NOT IN (a, b)` |
| `.between(X, a, b)` | `col BETWEEN a AND b` |
| `.notBetween(X, a, b)` | `col NOT BETWEEN a AND b` |
| `.orderByAsc(X)` | `ORDER BY col ASC` |
| `.orderByDesc(X)` | `ORDER BY col DESC` |
| `.limit(n)` | `LIMIT n` |
| `.limit(offset, n)` | MySQL: `LIMIT offset, n` / PostgreSQL: `LIMIT n OFFSET offset` |
| `.and(w -> ...)` | `AND (...)` |
| `.or()` | 下一个条件使用 `OR` 连接 |

## 使用示例

表名：

```text
grid_connection
```

输入：

```java
queryWrapper
    .eq(GridConnection::getStationId, stationId)
    .and(w -> w.eq(GridConnection::getType, type)
               .or()
               .eq(GridConnection::getStatus, status))
    .orderByDesc(GridConnection::getCreateTime)
    .limit(10);
```

选择 MySQL 后输出：

```sql
SELECT * FROM `grid_connection`
WHERE `station_id` = 'stationId'
  AND (
    `type` = 'type'
    OR `status` = 'status'
  )
ORDER BY `create_time` DESC
LIMIT 10
```

选择 PostgreSQL 时，表名和字段名会改用双引号，并按 PostgreSQL 规则生成分页语法。

## 运行方式

### 使用 Release 包

下载 GitHub Release 中的系统安装包后直接运行。

### 使用 jar

本机需要安装 JDK 17 或更高版本：

```bash
java -jar lambda2sql.jar
```

## 本地开发

环境要求：

- JDK 17+
- Maven 3.6+

运行测试：

```bash
mvn test
```

构建 jar：

```bash
mvn package
```

运行 jar：

```bash
java -jar target/lambda2sql.jar
```

## 说明

- Java 变量值无法在静态代码片段中获取真实运行时内容，因此会按变量名字符串处理，例如 `stationId` 会输出为 `'stationId'`。
- 当前暂未支持 `last(...)`、动态条件参数和集合变量展开。
- 复杂 Java 表达式、方法调用参数和动态条件暂未完整支持。

## License

MIT License
