package io.zrz.visitors.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * annotation to mark visitable interfaces
 *
 * The base interface for the visitable type should be annotated
 * with @Visitable.Base.
 *
 * Each type that is to be visitable needs to be annotated with @Visitable.Type.
 *
 */

@Documented
@Retention(SOURCE)
@Target(TYPE)
public @interface Visitable {

  /**
   * indicates a base interface for a visitable heirachy.
   */

  @Documented
  @Target(ElementType.TYPE)
  @Retention(SOURCE)
  public @interface Base {

    Class<?>[] value() default {};

    Visitor[] visitors() default {};

  }

  /**
   * Indicates that the class annotated with this method should be visitable.
   */

  @Documented
  @Target(ElementType.TYPE)
  @Retention(SOURCE)
  public @interface Type {
    String value() default "";

  }

  /**
   * defines a visitor type.
   */

  @Documented
  @Target(ElementType.TYPE)
  @Retention(SOURCE)
  public @interface Visitor {

    /**
     * A @FunctionalInterface to use as the prototype visit method.
     */

    Class<?> value() default Object.class;

    /**
     * The parameter number that will receive the visited object. Defaults to
     * the first parameter. If -1 is specified, then no visiting parameter will
     * be generated.
     */

    int bindParam() default 0;

    /**
     * The functional interface that will be returned when binding a visitable
     * to a visitor. It must match the parameters of the source visitable
     * interface, minus the parameter for the visitable.
     */

    Class<?> bindType() default Object.class;

    /**
     * The class name of the generated visitor.
     */

    String className() default "";

    /**
     * The package name to write the class to.
     */

    String packageName() default "";

  }

}
