package io.zrz.visitors.generators;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;

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

/**
 * Generates a helper enum with each of the visitable types, and a method which
 * converts instances to the key.
 *
 * @author theo
 *
 */

@Builder
@Wither
public class ObjectToTypeEnumGenerator implements VisitorImplementationGenerator {

  @Override
  public TypeSpec generate(VisitorSpec spec, GeneratorContext ctx) {

    final TypeSpec.Builder builder = TypeSpec.enumBuilder("MyEnumSwitch");

    builder.addModifiers(Modifier.PUBLIC);
    builder.addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "$S", "io.zrz.visitors").build());
    builder.addMethods(this.additionalMethods(builder, ctx, spec));

    for (final TypeName type : ctx.getTypes()) {

      final TypeSpec.Builder etype = TypeSpec.anonymousClassBuilder("");

      // invoke call
      {

        final MethodSpec.Builder method = MethodSpec.methodBuilder("invoke")
            // .addParameters(spec.parameters(ctx.getInterfaceType()))
            .addParameter(ParameterSpec.builder(ctx.getInterfaceType(), "visitable").build())
            .addParameter(ParameterSpec.builder(ClassName.bestGuess("MyNodeVisitors"), "visitor").build())
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.VOID);

        final CodeBlock.Builder cb = CodeBlock.builder();

        // cb.addStatement("$L typed = ($L)value", type, type);
        cb.addStatement("visitor.visit$L(($L)visitable)", ((ClassName) type).simpleName(), type);

        method.addCode(cb.build());

        etype.addMethod(method.build());

      }
      // bind call
      {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("bind")
            // .addParameters(spec.parameters(ctx.getInterfaceType()))
            .addParameter(ParameterSpec.builder(ClassName.bestGuess("MyNodeVisitors"), "visitor").build())
            .addModifiers(Modifier.PUBLIC)
            .returns(ParameterizedTypeName.get(ClassName.get(Consumer.class), type));
        final CodeBlock.Builder cb = CodeBlock.builder();
        // cb.addStatement("$L typed = ($L)value", type, type);
        cb.addStatement("return (visitable) -> visitor.visit$L(visitable)", ((ClassName) type).simpleName());
        method.addCode(cb.build());
        etype.addMethod(method.build());
      }

      // bind call
      {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("bind")
            // .addParameters(spec.parameters(ctx.getInterfaceType()))
            .addParameter(ParameterSpec.builder(ClassName.bestGuess("MyNodeVisitors"), "visitor").build())
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(TypeVariableName.get("R"))
            .returns(ParameterizedTypeName.get(ClassName.get(Function.class), type, TypeVariableName.get("R")));
        final CodeBlock.Builder cb = CodeBlock.builder();
        // cb.addStatement("$L typed = ($L)value", type, type);
        cb.addStatement("return (visitable) -> visitor.visit$L(visitable)", ((ClassName) type).simpleName());
        method.addCode(cb.build());
        etype.addMethod(method.build());
      }

      builder.addEnumConstant(((ClassName) type).simpleName(), etype.build());

      // builder.addMethod(this.generateMethod(spec, type));
    }

    final MethodSpec.Builder method = MethodSpec.methodBuilder("invoke")
        .addParameters(spec.parameters(ctx.getInterfaceType()))
        .addParameter(ParameterSpec.builder(ClassName.bestGuess("MyNodeVisitors"), "visitor").build())
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(TypeName.VOID);
    builder.addMethod(method.build());

    builder.addMethods(this.castAndInvokeMethod(builder, ctx, spec));

    return builder.build();
  }

  private Iterable<MethodSpec> additionalMethods(TypeSpec.Builder builder, GeneratorContext ctx, VisitorSpec spec) {

    final List<MethodSpec> methods = new LinkedList<>();

    // the default method

    final MethodSpec.Builder method = MethodSpec.methodBuilder("lookup")
        .addParameters(spec.parameters(ctx.getInterfaceType()))
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .returns(ClassName.bestGuess("MyEnumSwitch"));

    final CodeBlock.Builder cb = CodeBlock.builder();

    int count = 0;

    for (final TypeName type : ctx.getTypes()) {

      if (count++ == 0) {
        cb.beginControlFlow("if (value instanceof $L)", type);
      } else {
        cb.nextControlFlow("else if (value instanceof $L)", type);
      }

      cb.addStatement("return $L", ((ClassName) type).simpleName());

    }

    cb.endControlFlow();

    cb.addStatement("return null");

    method.addCode(cb.build());

    methods.add(method.build());
    return methods;
  }

  private Iterable<MethodSpec> castAndInvokeMethod(TypeSpec.Builder builder, GeneratorContext ctx, VisitorSpec spec) {

    final List<MethodSpec> methods = new LinkedList<>();

    // the default method

    final MethodSpec.Builder method = MethodSpec.methodBuilder("apply")
        .addParameter(ParameterSpec.builder(ctx.getInterfaceType(), "visitable").build())
        .addParameter(ParameterSpec.builder(ClassName.bestGuess("MyNodeVisitors"), "visitor").build())
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .returns(TypeName.VOID);

    final CodeBlock.Builder cb = CodeBlock.builder();

    int count = 0;

    for (final TypeName type : ctx.getTypes()) {

      if (count++ == 0) {
        cb.beginControlFlow("if (visitable instanceof $L)", type);
      } else {
        cb.nextControlFlow("else if (visitable instanceof $L)", type);
      }

      cb.addStatement("$L.invoke(($L)visitable, visitor)", ((ClassName) type).simpleName(), type);

    }

    cb.nextControlFlow("else");
    cb.addStatement("throw new java.lang.RuntimeException()");
    cb.endControlFlow();

    method.addCode(cb.build());

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
