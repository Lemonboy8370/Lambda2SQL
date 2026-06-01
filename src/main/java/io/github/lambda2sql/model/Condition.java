package io.github.lambda2sql.model;

/**
 * WHERE 条件模型。
 *
 * @param connector 当前条件与前一个条件之间的连接符
 * @param column 数据库列名，不包含方言引号
 * @param operator SQL 操作符，例如 = 或 >
 * @param valueSql 已格式化的 SQL 值，例如 '张三' 或 18
 */
public record Condition(String connector, String column, String operator, String valueSql) implements WherePart {
}
