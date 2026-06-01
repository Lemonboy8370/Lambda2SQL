package io.github.lambda2sql.model;

/**
 * ORDER BY 排序模型。
 *
 * @param column 数据库列名，不包含方言引号
 * @param direction 排序方向，例如 ASC 或 DESC
 */
public record Ordering(String column, String direction) {
}
