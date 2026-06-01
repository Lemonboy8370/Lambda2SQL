package io.github.lambda2sql.model;

/**
 * SQL 生成结果。
 *
 * <p>目前只包含 SQL 文本，后续可以扩展为携带警告、参数或解析信息。</p>
 *
 * @param sql 生成后的完整 SQL
 */
public record SqlResult(String sql) {
}
