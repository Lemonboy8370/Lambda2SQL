package io.github.lambda2sql.core;

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
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return "'" + value.replace("'", "''") + "'";
    }
}
