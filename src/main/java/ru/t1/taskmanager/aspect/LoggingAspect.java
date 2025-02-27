package ru.t1.taskmanager.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("@annotation(ru.t1.taskmanager.aspect.annotation.LogBefore)")
    public void logBefore() {
        logger.info("Перед вызовом метода");
    }

    @AfterReturning("@annotation(ru.t1.taskmanager.aspect.annotation.LogAfterReturning)")
    public void logAfterReturning() {
        logger.info("Метод выполнен успешно");
    }

    @AfterThrowing(value = "@annotation(ru.t1.taskmanager.aspect.annotation.LogAfterThrowing)", throwing = "exception")
    public void logAfterThrowing(Exception exception) {
        logger.error("Ошибка при выполнении метода: {}", exception.getMessage());
    }

    @Around("@annotation(ru.t1.taskmanager.aspect.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) {
        long start = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        long elapsedTime = System.currentTimeMillis() - start;
        logger.info("Метод {} выполнен за {} мс", joinPoint.getSignature(), elapsedTime);
        return result;
    }
}