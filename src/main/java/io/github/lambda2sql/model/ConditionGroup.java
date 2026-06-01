package io.github.lambda2sql.model;

import java.util.List;

/**
 * 带括号的 WHERE 条件组。
 *
 * @param connector 当前条件组与前一个条件之间的连接符
 * @param parts 条件组内部的条件片段
 */
public record ConditionGroup(String connector, List<WherePart> parts) implements WherePart {
}
