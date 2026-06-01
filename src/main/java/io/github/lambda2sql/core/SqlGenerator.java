package io.github.lambda2sql.core;

import io.github.lambda2sql.model.Condition;
import io.github.lambda2sql.model.ConditionGroup;
import io.github.lambda2sql.model.Ordering;
import io.github.lambda2sql.model.SqlResult;
import io.github.lambda2sql.model.WherePart;

import java.util.List;

/**
 * 将解析后的 Lambda 条件拼装成完整 SQL。
 *
 * <p>这是 core 包的对外入口：UI 层只需要调用 {@link #generate(String, Dialect, String)}，
 * 不需要知道解析和格式化的细节。</p>
 */
public final class SqlGenerator {
    /**
     * 负责把 Lambda 文本拆成结构化条件。
     */
    private final LambdaParser parser;

    public SqlGenerator() {
        this(new LambdaParser());
    }

    SqlGenerator(LambdaParser parser) {
        this.parser = parser;
    }

    /**
     * 根据表名、方言和 Lambda 条件生成完整 SELECT SQL。
     *
     * @param tableName 表名
     * @param dialect SQL 方言
     * @param lambdaInput Lambda 条件文本
     * @return SQL 生成结果
     */
    public SqlResult generate(String tableName, Dialect dialect, String lambdaInput) {
        validateTableName(tableName);
        LambdaParser.ParsedLambda parsed = parser.parse(lambdaInput);

        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(dialect.quoteIdentifier(tableName.trim()));

        appendWhere(sql, parsed.whereParts(), dialect);
        appendOrderBy(sql, parsed.orderings(), dialect);
        if (parsed.limit() != null) {
            sql.append('\n').append(dialect.limitClause(parsed.limit().offset(), parsed.limit().size()));
        }
        return new SqlResult(sql.toString());
    }

    /**
     * 拼接 WHERE 子句。第一条片段使用 WHERE，后续片段使用自身连接符。
     */
    private static void appendWhere(StringBuilder sql, List<WherePart> whereParts, Dialect dialect) {
        for (int i = 0; i < whereParts.size(); i++) {
            WherePart part = whereParts.get(i);
            sql.append(i == 0 ? "\nWHERE " : "\n  " + part.connector() + " ");
            appendWherePart(sql, part, dialect, "  ");
        }
    }

    /**
     * 拼接单个 WHERE 片段。片段可以是普通条件，也可以是括号包裹的条件组。
     */
    private static void appendWherePart(StringBuilder sql, WherePart part, Dialect dialect, String indent) {
        if (part instanceof Condition condition) {
            appendCondition(sql, condition, dialect);
            return;
        }
        if (part instanceof ConditionGroup group) {
            appendConditionGroup(sql, group, dialect, indent);
            return;
        }
        throw new IllegalArgumentException("不支持的 WHERE 片段：" + part.getClass().getName());
    }

    /**
     * 拼接普通条件。
     */
    private static void appendCondition(StringBuilder sql, Condition condition, Dialect dialect) {
        sql.append(dialect.quoteIdentifier(condition.column()))
                .append(' ')
                .append(condition.operator())
                .append(' ')
                .append(condition.valueSql());
    }

    /**
     * 拼接条件组，并在组外保留括号以维持 AND/OR 优先级。
     */
    private static void appendConditionGroup(StringBuilder sql, ConditionGroup group, Dialect dialect, String indent) {
        sql.append('(');
        String childIndent = indent + "  ";
        for (int i = 0; i < group.parts().size(); i++) {
            WherePart child = group.parts().get(i);
            sql.append('\n').append(childIndent);
            if (i > 0) {
                sql.append(child.connector()).append(' ');
            }
            appendWherePart(sql, child, dialect, childIndent);
        }
        sql.append('\n').append(indent).append(')');
    }

    /**
     * 拼接 ORDER BY 子句，多个排序字段用逗号分隔。
     */
    private static void appendOrderBy(StringBuilder sql, List<Ordering> orderings, Dialect dialect) {
        if (orderings.isEmpty()) {
            return;
        }
        sql.append("\nORDER BY ");
        for (int i = 0; i < orderings.size(); i++) {
            Ordering ordering = orderings.get(i);
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(dialect.quoteIdentifier(ordering.column())).append(' ').append(ordering.direction());
        }
    }

    /**
     * 校验表名，只允许普通字母、数字和下划线，避免用户输入直接破坏 SQL 结构。
     */
    private static void validateTableName(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("请输入表名");
        }
        if (!tableName.trim().matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("表名包含不支持的字符");
        }
    }
}
