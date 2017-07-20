package io.zrz.visitors.generators;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;

import com.google.common.collect.Lists;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import io.zrz.visitors.GeneratorContext;
import io.zrz.visitors.GeneratorUtils;
import io.zrz.visitors.VisitorImplementationGenerator;
import io.zrz.visitors.VisitorSpec;
import lombok.Builder;
import lombok.experimental.Wither;

@Builder
@Wither
public class DefaultVisitorImplGenerator implements VisitorImplementationGenerator {

  private final Function<VisitorSpec, ClassName> name;
  private final Function<VisitorSpec, CodeBlock> defaultBody;
  private final Function<VisitorSpec, TypeName> extendsType;
  private final Predicate<TypeName> filterTypes;

  @Override
  public TypeSpec generate(VisitorSpec spec, GeneratorContext ctx) {

    final TypeSpec.Builder builder = TypeSpec.classBuilder(this.name.apply(spec));

    builder.addModifiers(Modifier.STATIC);
    builder.addModifiers(Modifier.PUBLIC);

    if (this.extendsType != null) {
      builder.superclass(this.extendsType.apply(spec));
    }

    if (spec.typeVariables().isEmpty()) {
      builder.addSuperinterface(spec.getName());
    } else {
      final List<TypeVariableName> params = Lists.newArrayList(spec.typeVariables());
      builder.addSuperinterface(ParameterizedTypeName.get(spec.getName(), params.toArray(new TypeVariableName[0])));
    }

    if (this.defaultBody == null) {
      builder.addModifiers(Modifier.ABSTRACT);
    }

    builder.addTypeVariables(spec.typeVariables());

    builder.addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "$S", "io.zrz.visitors").build());

    builder.addMethods(this.additionalMethods(builder, ctx, spec));

    for (final TypeName type : ctx.getTypes()) {
      if (this.filterTypes == null || this.filterTypes.test(type)) {
        builder.addMethod(this.generateMethod(spec, type));
      }
    }

    return builder.build();
  }

  private Iterable<MethodSpec> additionalMethods(TypeSpec.Builder builder, GeneratorContext ctx, VisitorSpec spec) {

    final List<MethodSpec> methods = new LinkedList<>();

    // the constrcutor

    if (this.defaultBody != null && spec.returnType() != TypeName.VOID) {

      builder.addField(spec.returnType(), "_defaultValue", Modifier.PROTECTED, Modifier.FINAL);

      final MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
          .addParameter(ParameterSpec.builder(spec.returnType(), "defaultValue").build())
          .addModifiers(Modifier.PUBLIC)
          .addCode(CodeBlock.builder().addStatement("this._defaultValue = defaultValue").build());

      methods.add(constructor.build());

      final MethodSpec.Builder constructor1 = MethodSpec.constructorBuilder()
          .addModifiers(Modifier.PUBLIC)
          .addCode(CodeBlock.builder().addStatement("this._defaultValue = $L", this.defaultBody.apply(spec).toString()).build());

      methods.add(constructor1.build());

    }

    // the default method

    final MethodSpec.Builder method = MethodSpec.methodBuilder("visitDefault")
        .addParameters(spec.parameters(ctx.getInterfaceType()))
        .addModifiers(Modifier.PROTECTED)
        .returns(spec.returnType());

    if (this.defaultBody == null) {
      method.addModifiers(Modifier.ABSTRACT);
    } else {
      if (spec.returnType() != TypeName.VOID) {
        method.addCode(CodeBlock.builder().addStatement("return this._defaultValue").build());
      }
    }

    methods.add(method.build());
    return methods;
  }

  private MethodSpec generateMethod(VisitorSpec spec, TypeName type) {
    final MethodSpec.Builder method = MethodSpec.methodBuilder(GeneratorUtils.methodName(spec, type))
        .addModifiers(Modifier.PUBLIC)
        .addParameters(spec.parameters(type))
        .addAnnotation(AnnotationSpec.builder(Override.class).build())
        .returns(spec.returnType());

    this.applyBody(method, spec, type);
    return method.build();
  }

  private void applyBody(MethodSpec.Builder method, VisitorSpec spec, TypeName type) {
    final String args = spec.getMethodParameters().stream()
        .map(p -> p.apply(type))
        .map(p -> p.name)
        .collect(Collectors.joining(", "));

    final TypeName returnType = spec.returnType();

    if (returnType == TypeName.VOID) {
      method.addCode(CodeBlock.of("visitDefault($L);\n", args));
    } else {
      method.addCode(CodeBlock.of("return visitDefault($L);\n", args));
    }

  }
}
