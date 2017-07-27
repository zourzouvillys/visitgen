package io.zrz.visitors;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import io.zrz.visitors.VisitorSpec.VisitorSpecBuilder;

/**
 * takes a spec file, and generates the implementations.
 */

public class Main {

  /**
   * Pass in a functional interface, get out a VisitorSpec.
   *
   * @param funcintf
   * @return
   */

  public VisitorSpec fromFunctionalInterface(Class<?> funcintf) {

    return this.fromFunctionalInterface(funcintf, -1, null);

    // return VisitorSpec.builder()
    // .name(ClassName.get(this.packageName, "MyNodeVisitors",
    // funcintf.getSimpleName()))
    // .methodNamePrefix("test")
    // .typeParameter("T")
    // .methodParameter(typeVarParam("T", "t"))
    // .returnType(TypeName.BOOLEAN)
    // .build();

  }

  private ClassName makeName(String string) {

    return ClassName.get("io.zrz.test", "MyNodeVisitors", string + "Visitor");

  }

  /**
   * Generates a visitor from a functional interface, using the given parameter
   * number as the input, and using the specified class as the bound type.
   *
   * @param funcintf
   * @param bindParamNumber
   * @return
   */

  public VisitorSpec fromFunctionalInterface(Class<?> funcintf, int bindParamNumber, Class<?> boundType) {

    final Method method = GeneratorUtils.lookup(funcintf);

    final TypeName returnType = TypeName.get(method.getGenericReturnType());

    final VisitorSpecBuilder vb = VisitorSpec.builder()
        .name(this.makeName(funcintf.getSimpleName()))
        .methodNamePrefix(method.getName())
        .returnType(returnType);

    if (boundType != null) {
      vb.boundType(ClassName.get(boundType));
    }

    for (final Type t : method.getGenericParameterTypes()) {

      final TypeName tt = TypeName.get(t);

      if (tt instanceof TypeVariableName) {

        vb.typeParameter((TypeVariableName) tt);

      }

    }

    for (int i = 0; i < method.getParameterCount(); ++i) {

      if (i == bindParamNumber) {
        vb.methodParameter(VisitorGenerator.param("value"));
      } else {
        vb.methodParameter(TypeName.get(method.getGenericParameterTypes()[i]), method.getParameters()[i].getName());
      }

    }

    if (returnType instanceof TypeVariableName) {
      vb.typeParameter((TypeVariableName) returnType);
    }

    return vb.build();
  }

  public VisitorSpec fromFunctionalInterface(Class<?> funcintf, Class<?> boundType) {
    return this.fromFunctionalInterface(funcintf, 0, boundType);
  }

  public void run() {

    final VisitorSpec[] aspecs = {

        // this.fromFunctionalInterface(Predicate.class),
        this.fromFunctionalInterface(Predicate.class, 0, BooleanSupplier.class),
        // this.fromFunctionalInterface(BiPredicate.class),
        this.fromFunctionalInterface(BiPredicate.class, 0, Predicate.class),

        // this.fromFunctionalInterface(Consumer.class),
        this.fromFunctionalInterface(Consumer.class, Runnable.class),

        this.fromFunctionalInterface(BiConsumer.class, Consumer.class),
        this.fromFunctionalInterface(Supplier.class),
        this.fromFunctionalInterface(IntSupplier.class),
        this.fromFunctionalInterface(BooleanSupplier.class),
        this.fromFunctionalInterface(IntUnaryOperator.class),
        this.fromFunctionalInterface(ObjIntConsumer.class, IntConsumer.class),
        this.fromFunctionalInterface(ToIntFunction.class, IntSupplier.class),
        this.fromFunctionalInterface(IntToLongFunction.class, Runnable.class),
        this.fromFunctionalInterface(Function.class, Supplier.class),
        this.fromFunctionalInterface(BiFunction.class, Function.class),

    };

  }

  public static void main(String[] args) {

  }

}
