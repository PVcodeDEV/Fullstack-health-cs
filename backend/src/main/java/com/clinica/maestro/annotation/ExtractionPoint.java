package com.clinica.maestro.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents an extraction boundary for future module extraction.
 * Used to mark types, packages, or modules that are designed to be
 * extracted into a separate module in a future iteration.
 */
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ExtractionPoint {
    String value() default "";
}
