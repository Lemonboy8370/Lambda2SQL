package io.github.lambda2sql.core;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 将 Lambda 参数值格式化为 SQL 字面量。
 *
 * <p>当前规则很轻量：数字不加引号，null 转成 NULL，其他值按字符串处理并转义单引号。</p>
 */
final class ValueFormatter {
    private ValueFormatter() {
    }

    /**
     * 格式化单个参数值。
     *
     * @param rawValue Lambda 调用里拿到的原始参数文本
     * @return 可拼接进 SQL 的值文本
     */
    static String format(String rawValue) {
        String value = rawValue.trim();
        if (value.equalsIgnoreCase("null")) {
            return "NULL";
        }
        if (value.matches("-?\\d+(\\.\\d+)?")) {
            return value;
        }
        value = unquote(value);
        return "'" + value.replace("'", "''") + "'";
    }

    /**
     * 格式化 LIKE 参数，并按需要补齐 % 通配符。
     */
    static String formatLike(String rawValue, boolean leftWildcard, boolean rightWildcard) {
        String value = unquote(rawValue.trim());
        String pattern = (leftWildcard ? "%" : "") + value + (rightWildcard ? "%" : "");
        return "'" + pattern.replace("'", "''") + "'";
    }

    /**
     * 格式化 IN / NOT IN 参数列表。
     */
    static String formatList(List<String> rawValues) {
        return rawValues.stream()
                .map(ValueFormatter::format)
                .collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * 去掉字符串参数的外层单双引号。
     */
    private static String unquote(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
