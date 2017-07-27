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
   * indicates a base interface for a visitable hierarchy.
   */

  @Documented
  @Target(ElementType.TYPE)
  @Retention(SOURCE)
  public @interface Base {

    Visitor[] value() default {};

    /**
     * the package name for the Invoker and enum of types to be placed in. if
     * empty, uses the package that of the type this annotation is on.
     */

    String packageName() default "";

    /**
     * the class name to place the Invoker and enum in. If empty, defaults to
     * the name of the type annotated with this annotation and "Visitors"
     * appended.
     */

    String className() default "";

  }

  /**
   * Indicates that the class annotated with this method should be visitable.
   */

  @Documented
  @Target(ElementType.TYPE)
  @Retention(SOURCE)
  public @interface Type {

    /**
     * The name to use for this type in the visitor method names. If not
     * defined, defaults to the class name the annotation is on.
     *
     * <pre>
     * void visitMyType(MyType visitable);
     *
     * void visitMyOtherType(MyOtherType visitable);
     * </pre>
     *
     */

    String value() default "";

    /**
     * The name of the parameter to pass to the visiting method. If not defined,
     * defaults to "visitable".
     */

    String paramName() default "";

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
     * be provided to the visit methods.
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
