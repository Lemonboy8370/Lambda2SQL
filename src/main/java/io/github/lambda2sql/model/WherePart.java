package io.github.lambda2sql.model;

/**
 * WHERE 子句中的一个逻辑片段。
 *
 * <p>片段可以是单个条件，也可以是一个带括号的条件组。</p>
 */
public interface WherePart {
    /**
     * 当前片段和前一个片段之间的连接符，例如 AND 或 OR。
     */
    String connector();
}
