package io.zrz.haimod.api;

import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated element must not be {@code null}. Accepts any type.
 */

@Retention(RUNTIME)
@Documented
@Target(value = { TYPE_USE, TYPE_PARAMETER })
public @interface ListValueNotNull {

}
