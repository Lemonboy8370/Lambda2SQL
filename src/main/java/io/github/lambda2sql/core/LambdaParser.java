package io.github.lambda2sql.core;

import io.github.lambda2sql.model.Condition;
import io.github.lambda2sql.model.ConditionGroup;
import io.github.lambda2sql.model.Ordering;
import io.github.lambda2sql.model.WherePart;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析 MyBatis-Plus 风格的 Lambda 条件链文本。
 *
 * <p>解析器不直接生成 SQL，而是把输入拆成条件、排序和分页等结构化数据，
 * 交给 {@link SqlGenerator} 统一拼装。</p>
 */
public final class LambdaParser {
    /**
     * 解析多行 Lambda 条件文本。
     *
     * @param input 用户粘贴的 Lambda 条件链
     * @return 解析后的结构化结果
     */
    public ParsedLambda parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("请输入 Lambda 条件");
        }

        ParseOutput output = parseCalls(extractSupportedCalls(input));

        if (output.whereParts().isEmpty() && output.orderings().isEmpty() && output.limit() == null) {
            throw new IllegalArgumentException("未识别到有效条件");
        }
        return new ParsedLambda(output.whereParts(), output.orderings(), output.limit());
    }

    /**
     * 将扫描出来的方法调用转换成 WHERE 片段、排序和分页。
     */
    private static ParseOutput parseCalls(List<RawCall> calls) {
        List<WherePart> whereParts = new ArrayList<>();
        List<Ordering> orderings = new ArrayList<>();
        Limit limit = null;
        String nextConnector = "AND";

        for (RawCall call : calls) {
            List<String> args = splitArguments(call.args());
            switch (call.method()) {
                case "eq" -> {
                    whereParts.add(binary(args, nextConnector, "="));
                    nextConnector = "AND";
                }
                case "gt" -> {
                    whereParts.add(binary(args, nextConnector, ">"));
                    nextConnector = "AND";
                }
                case "and" -> {
                    ParseOutput nested = parseCalls(extractSupportedCalls(lambdaBody(call.args())));
                    if (!nested.whereParts().isEmpty()) {
                        whereParts.add(new ConditionGroup("AND", nested.whereParts()));
                    }
                    orderings.addAll(nested.orderings());
                    if (nested.limit() != null) {
                        limit = nested.limit();
                    }
                    nextConnector = "AND";
                }
                case "or" -> {
                    if (call.args().isBlank()) {
                        nextConnector = "OR";
                    } else {
                        ParseOutput nested = parseCalls(extractSupportedCalls(lambdaBody(call.args())));
                        if (!nested.whereParts().isEmpty()) {
                            whereParts.add(new ConditionGroup("OR", nested.whereParts()));
                        }
                        orderings.addAll(nested.orderings());
                        if (nested.limit() != null) {
                            limit = nested.limit();
                        }
                        nextConnector = "AND";
                    }
                }
                case "orderByDesc" -> orderings.add(ordering(args, "DESC"));
                case "orderByAsc" -> orderings.add(ordering(args, "ASC"));
                case "limit" -> limit = limit(args);
                default -> throw new IllegalArgumentException("不支持的方法：" + call.method());
            }
        }

        return new ParseOutput(whereParts, orderings, limit);
    }

    /**
     * 从任意 Java 文本中提取当前支持的 wrapper 调用。
     *
     * <p>这样既能处理每行一个调用的纯片段，也能处理完整 Java 代码块，
     * 例如 queryWrapper.eq(...); 或 .eq(...).gt(...)。</p>
     */
    private static List<RawCall> extractSupportedCalls(String input) {
        List<RawCall> calls = new ArrayList<>();
        int index = 0;
        while (index < input.length()) {
            int dotIndex = input.indexOf('.', index);
            if (dotIndex < 0) {
                break;
            }

            int methodStart = dotIndex + 1;
            int methodEnd = readJavaIdentifier(input, methodStart);
            if (methodEnd == methodStart) {
                index = methodStart;
                continue;
            }

            String method = input.substring(methodStart, methodEnd);
            int openParen = skipWhitespace(input, methodEnd);
            if (openParen >= input.length() || input.charAt(openParen) != '(') {
                index = methodEnd;
                continue;
            }

            int closeParen = findClosingParen(input, openParen);
            if (closeParen < 0) {
                index = openParen + 1;
                continue;
            }

            if (isSupportedMethod(method)) {
                calls.add(new RawCall(method, input.substring(openParen + 1, closeParen)));
            }
            index = closeParen + 1;
        }
        return calls;
    }

    /**
     * 判断当前方法名是否属于已实现的 LambdaWrapper 操作。
     */
    private static boolean isSupportedMethod(String method) {
        return switch (method) {
            case "eq", "gt", "and", "or", "orderByDesc", "orderByAsc", "limit" -> true;
            default -> false;
        };
    }

    /**
     * 从指定位置读取一个 Java 标识符。
     */
    private static int readJavaIdentifier(String input, int start) {
        if (start >= input.length() || !Character.isJavaIdentifierStart(input.charAt(start))) {
            return start;
        }
        int index = start + 1;
        while (index < input.length() && Character.isJavaIdentifierPart(input.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * 跳过方法名和左括号之间可能存在的空白。
     */
    private static int skipWhitespace(String input, int start) {
        int index = start;
        while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * 找到与左括号配对的右括号，扫描时会跳过字符串字面量中的括号。
     */
    private static int findClosingParen(String input, int openParen) {
        int depth = 0;
        boolean inString = false;
        char quote = 0;
        for (int i = openParen; i < input.length(); i++) {
            char ch = input.charAt(i);
            if ((ch == '"' || ch == '\'') && (i == 0 || input.charAt(i - 1) != '\\')) {
                if (inString && ch == quote) {
                    inString = false;
                } else if (!inString) {
                    inString = true;
                    quote = ch;
                }
            }
            if (inString) {
                continue;
            }
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 取出 Consumer Lambda 的方法链主体，例如 w -> w.eq(...).or().eq(...)。
     */
    private static String lambdaBody(String rawArgs) {
        int arrowIndex = rawArgs.indexOf("->");
        if (arrowIndex < 0) {
            return rawArgs;
        }
        return rawArgs.substring(arrowIndex + 2);
    }

    /**
     * 解析二元条件，例如 .eq(User::getName, "张三")。
     */
    private static Condition binary(List<String> args, String connector, String operator) {
        requireArgCount(args, 2);
        return new Condition(connector, ColumnMapper.toColumnName(args.get(0)), operator, ValueFormatter.format(args.get(1)));
    }

    /**
     * 解析排序条件，例如 .orderByDesc(User::getCreateTime)。
     */
    private static Ordering ordering(List<String> args, String direction) {
        requireArgCount(args, 1);
        return new Ordering(ColumnMapper.toColumnName(args.get(0)), direction);
    }

    /**
     * 解析分页参数，支持 limit(n) 和 limit(offset, n) 两种形式。
     */
    private static Limit limit(List<String> args) {
        if (args.size() == 1) {
            return new Limit(0, Integer.parseInt(args.get(0).trim()));
        }
        if (args.size() == 2) {
            return new Limit(Integer.parseInt(args.get(0).trim()), Integer.parseInt(args.get(1).trim()));
        }
        throw new IllegalArgumentException("limit 需要 1 个或 2 个参数");
    }

    /**
     * 检查当前操作符是否拿到了预期数量的参数。
     */
    private static void requireArgCount(List<String> args, int expected) {
        if (args.size() != expected) {
            throw new IllegalArgumentException("参数数量错误：需要 " + expected + " 个，实际 " + args.size() + " 个");
        }
    }

    /**
     * 拆分方法调用中的参数。
     *
     * <p>这里不能简单按逗号 split，因为字符串参数里也可能包含逗号。
     * 因此需要跟踪当前是否处在字符串内部。</p>
     */
    private static List<String> splitArguments(String rawArgs) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        char quote = 0;
        for (int i = 0; i < rawArgs.length(); i++) {
            char ch = rawArgs.charAt(i);
            if ((ch == '"' || ch == '\'') && (i == 0 || rawArgs.charAt(i - 1) != '\\')) {
                if (inString && ch == quote) {
                    inString = false;
                } else if (!inString) {
                    inString = true;
                    quote = ch;
                }
            }
            if (ch == ',' && !inString) {
                args.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (!current.isEmpty()) {
            args.add(current.toString().trim());
        }
        return args;
    }

    /**
     * Lambda 文本解析后的完整结果。
     */
    public record ParsedLambda(List<WherePart> whereParts, List<Ordering> orderings, Limit limit) {
    }

    /**
     * 分页设置。offset 为起始偏移量，size 为返回条数。
     */
    public record Limit(int offset, int size) {
    }

    /**
     * 内部解析结果，用来在递归解析条件组时传递排序和分页信息。
     */
    private record ParseOutput(List<WherePart> whereParts, List<Ordering> orderings, Limit limit) {
    }

    /**
     * 从输入文本里扫描到的一次 wrapper 方法调用。
     */
    private record RawCall(String method, String args) {
    }
}
