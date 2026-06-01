package io.github.lambda2sql.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将 Java Lambda getter 方法引用转换成数据库列名。
 *
 * <p>例如 {@code User::getCreateTime} 会转换为 {@code create_time}，
 * {@code User::isActive} 会转换为 {@code active}。</p>
 */
public final class ColumnMapper {
    /**
     * 匹配 MyBatis-Plus Lambda 常见的方法引用格式：类名::getXxx 或 类名::isXxx。
     */
    private static final Pattern METHOD_REFERENCE = Pattern.compile("[A-Za-z_$][\\w$]*::(get|is)([A-Za-z_$][\\w$]*)");

    private ColumnMapper() {
    }

    /**
     * 把方法引用中的 getter 名称提取出来，并转换成下划线列名。
     *
     * @param methodReference 形如 {@code User::getName} 的方法引用文本
     * @return 转换后的数据库列名
     */
    public static String toColumnName(String methodReference) {
        Matcher matcher = METHOD_REFERENCE.matcher(methodReference.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("不支持的字段引用：" + methodReference);
        }
        return camelToSnake(matcher.group(2));
    }

    /**
     * 将驼峰命名转换为下划线命名，同时尽量保留连续大写缩写的词块边界。
     */
    private static String camelToSnake(String value) {
        String withWordBoundaries = value
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return withWordBoundaries.toLowerCase();
    }
}
