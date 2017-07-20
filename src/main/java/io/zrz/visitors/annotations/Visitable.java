package io.zrz.visitors.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target(TYPE)
public @interface Visitable {

  @Documented
  @Target(ElementType.TYPE)
  public @interface Base {
  }

  @Documented
  @Target(ElementType.TYPE)
  public @interface Type {
    String value() default "";
  }

}
