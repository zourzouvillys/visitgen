
package io.zrz.visitors;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * Spec for generated visitors.
 *
 * One for each type that is supported.
 *
 * @author theo
 *
 */

@Value
@Builder
public class VisitorSpec {

  private final ClassName name;
  private final TypeName returnType;
  private final Function<TypeName, String> methodName;
  private final String methodNamePrefix;
  private final String methodNameSuffix;
  private final List<Function<TypeName, ParameterSpec>> methodParameters;
  private final List<TypeVariableName> typeParameters;

  /**
   * The @FunctionalInterface type that is returned when binding a visitor to a
   * concrete instance.
   */

  private final TypeName boundType;

  @Singular
  private final List<VisitorImplementationGenerator> implementations;

  public static class VisitorSpecBuilder {

    private List<Function<TypeName, ParameterSpec>> methodParameters = Lists.newLinkedList();
    private List<TypeVariableName> typeParameters = Lists.newLinkedList();

    public VisitorSpecBuilder methodParameter(TypeName paramType, String paramName) {
      this.methodParameters.add(type -> ParameterSpec.builder(paramType, paramName).build());
      return this;
    }

    public VisitorSpecBuilder methodParameter(Function<TypeName, ParameterSpec> spec) {
      this.methodParameters.add(spec);
      return this;
    }

    public VisitorSpecBuilder name(String packageName, String simpleName, String... simpleNames) {
      this.name = ClassName.get(packageName, simpleName, simpleNames);
      return this;
    }

    public VisitorSpecBuilder name(ClassName name) {
      this.name = name;
      return this;
    }

    public VisitorSpecBuilder methodName(String name) {
      this.methodName = type -> name;
      return this;
    }

    public VisitorSpecBuilder methodName(final Function<TypeName, String> name) {
      this.methodName = name;
      return this;
    }

    public VisitorSpecBuilder typeParameter(TypeVariableName name) {
      this.typeParameters.add(name);
      return this;
    }

    public VisitorSpecBuilder typeParameter(String name) {
      this.typeParameters.add(TypeVariableName.get(name));
      return this;
    }

    public VisitorSpecBuilder genericReturnType(String name) {
      this.returnType = TypeVariableName.get(name);
      return this;
    }

  }

  public TypeName toType(Function<ClassName, ClassName> modifier) {
    if (this.typeVariables().isEmpty()) {
      return modifier.apply(this.name);
    }
    return ParameterizedTypeName.get(modifier.apply(this.name), this.typeVariables().toArray(new TypeVariableName[0]));
  }

  public TypeName returnType() {
    if (this.getReturnType() == null) {
      return TypeName.VOID;
    }
    return this.getReturnType();
  }

  public List<TypeVariableName> typeVariables() {
    return this.getTypeParameters();
  }

  public List<ParameterSpec> parameters(TypeName type) {
    return this.getMethodParameters().stream().map(supplier -> supplier.apply(type)).collect(Collectors.toList());
  }

  public String applyName() {
    return (this.methodNamePrefix == null ? "" : this.methodNamePrefix) + (this.methodNameSuffix == null ? "" : this.methodNameSuffix);
  }

  public String applyName(String insert) {
    return (this.methodNamePrefix == null ? "" : this.methodNamePrefix) + insert + (this.methodNameSuffix == null ? "" : this.methodNameSuffix);
  }

  public TypeName calculateBoundReturnType(ClassName klass) {

    if (this.getTypeParameters().size() <= 1) {
      return this.getBoundType();
    } else {
      return this.getBoundType();
    }

  }

  public Iterable<TypeVariableName> calculateBoundTypeVariables(ClassName klass) {
    return new LinkedList<>(this.typeVariables());
  }

}
