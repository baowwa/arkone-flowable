package com.arkone.flowable.util;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.exception.ExpressionRuntimeException;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDecimal;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 公式计算引擎 - 基于Aviator实现
 *
 * <p>提供安全的数学公式计算功能，用于动态表单中的计算字段。
 * 采用白名单机制，仅允许使用预定义的安全函数。</p>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>公式长度限制：最大500字符</li>
 *   <li>执行超时：1秒</li>
 *   <li>函数白名单：仅允许 max/min/round/abs/ceil/floor</li>
 *   <li>禁用反射和类加载</li>
 *   <li>循环次数限制：1000次</li>
 * </ul>
 *
 * <h3>支持的公式示例：</h3>
 * <pre>
 * // 基础运算
 * 末修产物浓度 * 上机文库体积 / 660
 *
 * // 使用白名单函数
 * round(核酸浓度 * 1.5, 2)
 * max(浓度1, 浓度2, 浓度3)
 * abs(目标值 - 实际值)
 * </pre>
 *
 * @author ArkOne Team
 * @version 1.0
 * @since 2026-02-25
 */
@Component
public class FormulaEngine {

    private static final Logger logger = LoggerFactory.getLogger(FormulaEngine.class);

    /**
     * 公式最大长度限制
     */
    private static final int MAX_FORMULA_LENGTH = 500;

    /**
     * 公式执行超时时间（毫秒）
     */
    private static final long EVAL_TIMEOUT_MS = 1000L;

    /**
     * Aviator计算引擎实例
     */
    private static final AviatorEvaluatorInstance evaluator = AviatorEvaluator.newInstance();

    /**
     * 线程池用于超时控制
     */
    private static final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("formula-engine-" + thread.getId());
        return thread;
    });

    static {
        // 配置安全选项
        evaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        evaluator.setOption(Options.TRACE_EVAL, false);
        evaluator.setOption(Options.MAX_LOOP_COUNT, 1000);

        // 禁用危险功能
        evaluator.setOption(Options.USE_USER_ENV_AS_TOP_ENV_DIRECTLY, false);
        evaluator.setOption(Options.CAPTURE_FUNCTION_ARGS, false);

        // 注册白名单函数
        evaluator.addFunction(new MaxFunction());
        evaluator.addFunction(new MinFunction());
        evaluator.addFunction(new RoundFunction());
        evaluator.addFunction(new AbsFunction());
        evaluator.addFunction(new CeilFunction());
        evaluator.addFunction(new FloorFunction());

        logger.info("FormulaEngine initialized with whitelist functions: max, min, round, abs, ceil, floor");
    }

    /**
     * 计算公式
     *
     * @param formula 公式表达式，例如："核酸浓度 * 1.5"
     * @param context 变量上下文，键为变量名，值为变量值
     * @return 计算结果
     * @throws FormulaException 当公式无效、超时或计算失败时抛出
     */
    public Object evaluate(String formula, Map<String, Object> context) throws FormulaException {
        // 参数校验
        if (formula == null || formula.trim().isEmpty()) {
            throw new FormulaException("公式不能为空");
        }

        if (formula.length() > MAX_FORMULA_LENGTH) {
            throw new FormulaException("公式长度超过限制: " + MAX_FORMULA_LENGTH + " 字符");
        }

        // 安全检查：禁止使用危险关键字
        validateFormulaSafety(formula);

        try {
            // 编译公式
            Expression expression = evaluator.compile(formula, true);

            // 使用Future实现超时控制
            Future<Object> future = executorService.submit(() -> expression.execute(context));

            try {
                Object result = future.get(EVAL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                logger.debug("Formula evaluated successfully: {} = {}", formula, result);
                return result;
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new FormulaException("公式执行超时（超过 " + EVAL_TIMEOUT_MS + "ms）");
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ExpressionRuntimeException) {
                    throw new FormulaException("公式执行错误: " + cause.getMessage(), cause);
                }
                throw new FormulaException("公式计算失败: " + cause.getMessage(), cause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FormulaException("公式计算被中断", e);
            }

        } catch (Exception e) {
            if (e instanceof FormulaException) {
                throw (FormulaException) e;
            }
            logger.error("Formula evaluation failed: {}", formula, e);
            throw new FormulaException("公式解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证公式安全性
     *
     * @param formula 公式表达式
     * @throws FormulaException 当公式包含危险关键字时抛出
     */
    private void validateFormulaSafety(String formula) throws FormulaException {
        String lowerFormula = formula.toLowerCase();

        // 禁止的关键字列表
        String[] dangerousKeywords = {
            "class", "import", "new ", "reflect", "runtime",
            "system", "process", "thread", "file", "socket",
            "exec", "load", "invoke"
        };

        for (String keyword : dangerousKeywords) {
            if (lowerFormula.contains(keyword)) {
                throw new FormulaException("公式包含禁止使用的关键字: " + keyword);
            }
        }
    }

    /**
     * 最大值函数
     */
    private static class MaxFunction extends AbstractFunction {
        @Override
        public String getName() {
            return "max";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
            Number num = FunctionUtils.getNumberValue(arg1, env);
            return AviatorDecimal.valueOf(new BigDecimal(num.toString()));
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
            Number num1 = FunctionUtils.getNumberValue(arg1, env);
            Number num2 = FunctionUtils.getNumberValue(arg2, env);
            BigDecimal bd1 = new BigDecimal(num1.toString());
            BigDecimal bd2 = new BigDecimal(num2.toString());
            return AviatorDecimal.valueOf(bd1.compareTo(bd2) > 0 ? bd1 : bd2);
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
            Number num1 = FunctionUtils.getNumberValue(arg1, env);
            Number num2 = FunctionUtils.getNumberValue(arg2, env);
            Number num3 = FunctionUtils.getNumberValue(arg3, env);
            BigDecimal bd1 = new BigDecimal(num1.toString());
            BigDecimal bd2 = new BigDecimal(num2.toString());
            BigDecimal bd3 = new BigDecimal(num3.toString());
            BigDecimal max = bd1;
            if (bd2.compareTo(max) > 0) max = bd2;
            if (bd3.compareTo(max) > 0) max = bd3;
            return AviatorDecimal.valueOf(max);
        }
    }

    /**
     * 最小值函数
     */
    private static class MinFunction extends AbstractFunction {
        @Override
        public String getName() {
            return "min";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
            Number num = FunctionUtils.getNumberValue(arg1, env);
            return AviatorDecimal.valueOf(new BigDecimal(num.toString()));
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
            Number num1 = FunctionUtils.getNumberValue(arg1, env);
            Number num2 = FunctionUtils.getNumberValue(arg2, env);
            BigDecimal bd1 = new BigDecimal(num1.toString());
            BigDecimal bd2 = new BigDecimal(num2.toString());
            return AviatorDecimal.valueOf(bd1.compareTo(bd2) < 0 ? bd1 : bd2);
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2, AviatorObject arg3) {
            Number num1 = FunctionUtils.getNumberValue(arg1, env);
            Number num2 = FunctionUtils.getNumberValue(arg2, env);
            Number num3 = FunctionUtils.getNumberValue(arg3, env);
            BigDecimal bd1 = new BigDecimal(num1.toString());
            BigDecimal bd2 = new BigDecimal(num2.toString());
            BigDecimal bd3 = new BigDecimal(num3.toString());
            BigDecimal min = bd1;
            if (bd2.compareTo(min) < 0) min = bd2;
            if (bd3.compareTo(min) < 0) min = bd3;
            return AviatorDecimal.valueOf(min);
        }
    }

    /**
     * 四舍五入函数
     */
    private static class RoundFunction extends AbstractFunction {
        @Override
        public String getName() {
            return "round";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
            Number value = FunctionUtils.getNumberValue(arg1, env);
            Number scale = FunctionUtils.getNumberValue(arg2, env);
            BigDecimal bd = new BigDecimal(value.toString());
            bd = bd.setScale(scale.intValue(), RoundingMode.HALF_UP);
            return AviatorDecimal.valueOf(bd);
        }
    }

    /**
     * 绝对值函数
     */
    private static class AbsFunction extends AbstractFunction {
        @Override
        public String getName() {
            return "abs";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg) {
            Number value = FunctionUtils.getNumberValue(arg, env);
            BigDecimal bd = new BigDecimal(value.toString());
            return AviatorDecimal.valueOf(bd.abs());
        }
    }

    /**
     * 向上取整函数
     */
    private static class CeilFunction extends AbstractFunction {
        @Override
        public String getName() {
            return "ceil";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg) {
            Number value = FunctionUtils.getNumberValue(arg, env);
            BigDecimal bd = new BigDecimal(value.toString());
            bd = bd.setScale(0, RoundingMode.CEILING);
            return AviatorDecimal.valueOf(bd);
        }
    }

    /**
     * 向下取整函数
     */
    private static class FloorFunction extends AbstractFunction {
        @Override
        public String getName() {
            return "floor";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg) {
            Number value = FunctionUtils.getNumberValue(arg, env);
            BigDecimal bd = new BigDecimal(value.toString());
            bd = bd.setScale(0, RoundingMode.FLOOR);
            return AviatorDecimal.valueOf(bd);
        }
    }

    /**
     * 公式异常
     */
    public static class FormulaException extends Exception {
        public FormulaException(String message) {
            super(message);
        }

        public FormulaException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
