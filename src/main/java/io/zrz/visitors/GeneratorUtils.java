package io.zrz.visitors;

import java.lang.reflect.Method;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

public class GeneratorUtils {

  /**
   * Evaluates an @FunctionalInterface and extracts the template paramrters,
   * method name, return type, and arguments.
   */

  public static Method lookup(Class<?> functionalInterface) {

    Preconditions.checkArgument(functionalInterface.isAnnotationPresent(FunctionalInterface.class));

    for (final Method method : functionalInterface.getDeclaredMethods()) {

      if (method.isDefault()) {
        continue;
      }

      return method;

    }

    throw new RuntimeException(String.format("Couldn't find single non default method in %s", functionalInterface.getCanonicalName()));

  }

  /**
   * Generates a method
   *
   * @param spec
   * @param type
   * @return
   */

  public static String methodName(VisitorSpec spec, TypeName type) {

    final StringBuilder sb = new StringBuilder();

    if (spec.getMethodNamePrefix() != null) {
      sb.append(spec.getMethodNamePrefix());
    }

    if (spec.getMethodName() != null) {
      sb.append(spec.getMethodName().apply(type));
    } else {
      sb.append(simpleName(type));
    }

    if (spec.getMethodNameSuffix() != null) {
      sb.append(spec.getMethodNameSuffix());
    }

    return sb.toString();
  }

  private static String simpleName(TypeName type) {

    if (type instanceof ClassName) {
      return ((ClassName) type).simpleName();
    } else if (type instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) type).rawType.simpleName();
    }

    return null;
  }

  public static CodeBlock defaultReturn(VisitorSpec spec) {
    if (spec.returnType() == TypeName.VOID) {
      return CodeBlock.builder().add("").build();
    }
    return CodeBlock.builder().add("null").build();
  }

  public static ClassName prefixName(String prefix, ClassName spec) {
    final List<String> simpleNames = Lists.newArrayList(spec.simpleNames());

    final String finalName = prefix + simpleNames.get(simpleNames.size() - 1);

    simpleNames.set(simpleNames.size() - 1, finalName);

    final String simpleName = simpleNames.remove(0);

    return ClassName.get(spec.packageName(), simpleName, simpleNames.toArray(new String[0]));

  }

  public static TypeName prefixName(String prefix, ClassName spec, List<TypeVariableName> typeVariables) {
    if (typeVariables.isEmpty()) {
      return prefixName(prefix, spec);
    }

    final List<TypeVariableName> retVars = Lists.newLinkedList(typeVariables);

    return ParameterizedTypeName.get(prefixName(prefix, spec), retVars.toArray(new TypeVariableName[0]));
  }

}
