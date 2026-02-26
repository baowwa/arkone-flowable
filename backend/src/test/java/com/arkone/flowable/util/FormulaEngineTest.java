package com.arkone.flowable.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FormulaEngine单元测试
 *
 * @author ArkOne Team
 * @version 1.0
 * @since 2026-02-25
 */
@DisplayName("公式引擎测试")
class FormulaEngineTest {

    private FormulaEngine formulaEngine;

    @BeforeEach
    void setUp() {
        formulaEngine = new FormulaEngine();
    }

    @Test
    @DisplayName("测试基础算术运算")
    void testBasicArithmetic() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 10);
        context.put("b", 5);

        // 加法
        Object result = formulaEngine.evaluate("a + b", context);
        assertEquals(15, ((Number) result).intValue());

        // 减法
        result = formulaEngine.evaluate("a - b", context);
        assertEquals(5, ((Number) result).intValue());

        // 乘法
        result = formulaEngine.evaluate("a * b", context);
        assertEquals(50, ((Number) result).intValue());

        // 除法
        result = formulaEngine.evaluate("a / b", context);
        assertEquals(2, ((Number) result).intValue());
    }

    @Test
    @DisplayName("测试复杂表达式")
    void testComplexExpression() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("末修产物浓度", 100.5);
        context.put("上机文库体积", 20);

        Object result = formulaEngine.evaluate("末修产物浓度 * 上机文库体积 / 660", context);
        assertNotNull(result);
        assertTrue(result instanceof Number);

        double expected = 100.5 * 20 / 660.0;
        double actual = ((Number) result).doubleValue();
        assertEquals(expected, actual, 0.0001);
    }

    @Test
    @DisplayName("测试max函数")
    void testMaxFunction() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("浓度1", 100);
        context.put("浓度2", 150);
        context.put("浓度3", 120);

        Object result = formulaEngine.evaluate("max(浓度1, 浓度2, 浓度3)", context);
        assertEquals(new BigDecimal("150"), result);
    }

    @Test
    @DisplayName("测试min函数")
    void testMinFunction() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("浓度1", 100);
        context.put("浓度2", 150);
        context.put("浓度3", 120);

        Object result = formulaEngine.evaluate("min(浓度1, 浓度2, 浓度3)", context);
        assertEquals(new BigDecimal("100"), result);
    }

    @Test
    @DisplayName("测试round函数")
    void testRoundFunction() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("核酸浓度", 125.5678);

        // 保留2位小数
        Object result = formulaEngine.evaluate("round(核酸浓度, 2)", context);
        assertEquals(new BigDecimal("125.57"), result);

        // 保留0位小数
        result = formulaEngine.evaluate("round(核酸浓度, 0)", context);
        assertEquals(new BigDecimal("126"), result);
    }

    @Test
    @DisplayName("测试abs函数")
    void testAbsFunction() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("目标值", 100);
        context.put("实际值", 95);

        Object result = formulaEngine.evaluate("abs(目标值 - 实际值)", context);
        assertEquals(new BigDecimal("5"), result);

        context.put("实际值", 105);
        result = formulaEngine.evaluate("abs(目标值 - 实际值)", context);
        assertEquals(new BigDecimal("5"), result);
    }

    @Test
    @DisplayName("测试ceil函数")
    void testCeilFunction() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("value", 10.3);

        Object result = formulaEngine.evaluate("ceil(value)", context);
        assertEquals(new BigDecimal("11"), result);

        context.put("value", 10.9);
        result = formulaEngine.evaluate("ceil(value)", context);
        assertEquals(new BigDecimal("11"), result);
    }

    @Test
    @DisplayName("测试floor函数")
    void testFloorFunction() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("value", 10.3);

        Object result = formulaEngine.evaluate("floor(value)", context);
        assertEquals(new BigDecimal("10"), result);

        context.put("value", 10.9);
        result = formulaEngine.evaluate("floor(value)", context);
        assertEquals(new BigDecimal("10"), result);
    }

    @Test
    @DisplayName("测试组合函数")
    void testCombinedFunctions() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 10.567);
        context.put("b", 20.234);
        context.put("c", 15.789);

        Object result = formulaEngine.evaluate("round(max(a, b, c), 2)", context);
        assertEquals(new BigDecimal("20.23"), result);

        result = formulaEngine.evaluate("abs(round(a - b, 1))", context);
        assertEquals(new BigDecimal("9.7"), result);
    }

    @Test
    @DisplayName("测试空公式")
    void testEmptyFormula() {
        Map<String, Object> context = new HashMap<>();

        FormulaEngine.FormulaException exception = assertThrows(
            FormulaEngine.FormulaException.class,
            () -> formulaEngine.evaluate("", context)
        );
        assertTrue(exception.getMessage().contains("公式不能为空"));

        exception = assertThrows(
            FormulaEngine.FormulaException.class,
            () -> formulaEngine.evaluate(null, context)
        );
        assertTrue(exception.getMessage().contains("公式不能为空"));
    }

    @Test
    @DisplayName("测试公式长度限制")
    void testFormulaLengthLimit() {
        Map<String, Object> context = new HashMap<>();

        // 生成超过500字符的公式
        StringBuilder longFormula = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longFormula.append("a + b + ");
        }
        longFormula.append("c");

        FormulaEngine.FormulaException exception = assertThrows(
            FormulaEngine.FormulaException.class,
            () -> formulaEngine.evaluate(longFormula.toString(), context)
        );
        assertTrue(exception.getMessage().contains("公式长度超过限制"));
    }

    @Test
    @DisplayName("测试危险关键字检测")
    void testDangerousKeywords() {
        Map<String, Object> context = new HashMap<>();

        String[] dangerousFormulas = {
            "java.lang.System.exit(0)",
            "Runtime.getRuntime()",
            "Class.forName('java.lang.String')",
            "new java.io.File('/etc/passwd')",
            "Thread.sleep(1000)"
        };

        for (String formula : dangerousFormulas) {
            FormulaEngine.FormulaException exception = assertThrows(
                FormulaEngine.FormulaException.class,
                () -> formulaEngine.evaluate(formula, context)
            );
            assertTrue(exception.getMessage().contains("禁止使用的关键字"));
        }
    }

    @Test
    @DisplayName("测试无效公式")
    void testInvalidFormula() {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 10);

        FormulaEngine.FormulaException exception = assertThrows(
            FormulaEngine.FormulaException.class,
            () -> formulaEngine.evaluate("a + + b", context)
        );
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("测试未定义变量")
    void testUndefinedVariable() {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 10);

        FormulaEngine.FormulaException exception = assertThrows(
            FormulaEngine.FormulaException.class,
            () -> formulaEngine.evaluate("a + undefinedVar", context)
        );
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("测试除零错误")
    void testDivisionByZero() {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 10);
        context.put("b", 0);

        FormulaEngine.FormulaException exception = assertThrows(
            FormulaEngine.FormulaException.class,
            () -> formulaEngine.evaluate("a / b", context)
        );
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("测试浮点数精度")
    void testFloatingPointPrecision() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 0.1);
        context.put("b", 0.2);

        Object result = formulaEngine.evaluate("a + b", context);
        assertTrue(result instanceof Number);

        // 验证结果接近0.3
        double actual = ((Number) result).doubleValue();
        assertEquals(0.3, actual, 0.0001);
    }

    @Test
    @DisplayName("测试中文变量名")
    void testChineseVariableNames() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("核酸浓度", 125.5);
        context.put("体积", 10);
        context.put("稀释倍数", 2);

        Object result = formulaEngine.evaluate("核酸浓度 * 体积 / 稀释倍数", context);
        assertNotNull(result);

        double expected = 125.5 * 10 / 2.0;
        double actual = ((Number) result).doubleValue();
        assertEquals(expected, actual, 0.0001);
    }

    @Test
    @DisplayName("测试负数运算")
    void testNegativeNumbers() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("a", -10);
        context.put("b", 5);

        Object result = formulaEngine.evaluate("a + b", context);
        assertEquals(-5, ((Number) result).intValue());

        result = formulaEngine.evaluate("abs(a)", context);
        assertEquals(10, ((Number) result).intValue());
    }

    @Test
    @DisplayName("测试括号优先级")
    void testParenthesesPrecedence() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 10);
        context.put("b", 5);
        context.put("c", 2);

        Object result1 = formulaEngine.evaluate("a + b * c", context);
        Object result2 = formulaEngine.evaluate("(a + b) * c", context);

        assertEquals(20, ((Number) result1).intValue());  // 10 + (5 * 2) = 20
        assertEquals(30, ((Number) result2).intValue());  // (10 + 5) * 2 = 30
    }

    @Test
    @DisplayName("测试空上下文")
    void testEmptyContext() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();

        // 常量表达式
        Object result = formulaEngine.evaluate("10 + 20", context);
        assertEquals(30, ((Number) result).intValue());

        result = formulaEngine.evaluate("max(10, 20, 15)", context);
        assertEquals(20, ((Number) result).intValue());
    }

    @Test
    @DisplayName("测试大数值计算")
    void testLargeNumbers() throws FormulaEngine.FormulaException {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 999999999);
        context.put("b", 888888888);

        Object result = formulaEngine.evaluate("a + b", context);
        assertEquals(1888888887L, ((Number) result).longValue());

        result = formulaEngine.evaluate("a * b", context);
        assertNotNull(result);
        assertTrue(result instanceof Number);
    }
}
