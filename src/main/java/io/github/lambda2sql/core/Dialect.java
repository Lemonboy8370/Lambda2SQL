package io.github.lambda2sql.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SQL 方言定义。
 *
 * <p>目前封装 MySQL 和 PostgreSQL 在标识符引号、分页语法上的差异。</p>
 */
@Getter
public enum Dialect {
    /**
     * MySQL 使用反引号包裹表名和字段名。
     */
    MYSQL("`"),

    /**
     * PostgreSQL 使用双引号包裹表名和字段名。
     */
    POSTGRESQL("\"");

    /**
     * 当前方言用于包裹标识符的符号。
     */
    private final String identifierQuote;

    Dialect(String identifierQuote) {
        this.identifierQuote = identifierQuote;
    }

    /**
     * 包裹表名或字段名，避免关键字或大小写问题。
     *
     * @param identifier 表名或字段名
     * @return 按当前方言加引号后的标识符
     */
    public String quoteIdentifier(String identifier) {
        return identifierQuote + identifier + identifierQuote;
    }

    /**
     * 生成当前方言的 LIMIT 子句。
     *
     * <p>当 offset 为 0 时，两个方言都输出 {@code LIMIT n}；
     * 当 offset 大于 0 时，MySQL 输出 {@code LIMIT offset, limit}，
     * PostgreSQL 输出 {@code LIMIT limit OFFSET offset}。</p>
     */
    public String limitClause(int offset, int limit) {
        if (offset < 0 || limit < 0) {
            throw new IllegalArgumentException("limit 参数不能为负数");
        }
        if (offset == 0) {
            return "LIMIT " + limit;
        }
        return switch (this) {
            case MYSQL -> "LIMIT " + offset + ", " + limit;
            case POSTGRESQL -> "LIMIT " + limit + " OFFSET " + offset;
        };
    }
}
