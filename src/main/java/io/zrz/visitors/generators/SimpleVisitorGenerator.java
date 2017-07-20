package io.zrz.visitors.generators;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import io.zrz.visitors.Generator;
import io.zrz.visitors.GeneratorUtils;
import io.zrz.visitors.VisitorSpec;

public class SimpleVisitorGenerator implements Generator {

  private final VisitorSpec spec;

  public SimpleVisitorGenerator(VisitorSpec spec) {
    this.spec = spec;
  }

  @Override
  public TypeSpec.Builder generate(Collection<TypeName> types) {

    final TypeSpec.Builder spec = TypeSpec.interfaceBuilder(this.spec.getName());

    spec.addModifiers(Modifier.STATIC);
    spec.addModifiers(Modifier.PUBLIC);

    spec.addTypeVariables(this.typeVariables());

    for (final TypeName type : types) {
      spec.addMethod(
          MethodSpec.methodBuilder(GeneratorUtils.methodName(this.spec, type))
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addParameters(this.parameters(type))
              .returns(this.returnType())
              .build());

    }

    return spec;

  }

  private TypeName returnType() {
    if (this.spec.getReturnType() == null) {
      return TypeName.VOID;
    }
    return this.spec.getReturnType();
  }

  private List<TypeVariableName> typeVariables() {
    return this.spec.getTypeParameters();
  }

  private List<ParameterSpec> parameters(TypeName type) {
    return this.spec.getMethodParameters().stream().map(supplier -> supplier.apply(type)).collect(Collectors.toList());
  }

}
