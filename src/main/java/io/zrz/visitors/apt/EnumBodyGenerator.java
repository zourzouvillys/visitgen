package io.zrz.visitors.apt;

import javax.lang.model.element.Modifier;

import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import io.zrz.visitors.apt.ReflectedVisitable.Type;
import io.zrz.visitors.apt.ReflectedVisitable.Visitor;
import io.zrz.visitors.apt.ReflectedVisitable.VisitorBinder;

public class EnumBodyGenerator {

  private final GenerationContext ctx;

  // builds the "apply" method in the enum constant

  public EnumBodyGenerator(GenerationContext ctx) {
    this.ctx = ctx;
  }

  /**
   * called once per visitable type. the returned typespec is the body of the
   * enum constant.
   */

  public TypeSpec generate(Type type) {
    final TypeSpec.Builder enumtype = TypeSpec.anonymousClassBuilder("");
    for (final Visitor spec : this.ctx.getBase().getVisitors()) {
      enumtype.addMethod(this.buildEnumConstantApply(type, spec));
    }
    return enumtype.build();
  }

  private MethodSpec buildEnumConstantApply(Type type, Visitor spec) {

    final VisitorBinder val = spec.getValue();

    final MethodSpec.Builder method = MethodSpec.methodBuilder(val.getInvocationName());
    method.addJavadoc(String.format("buildEnumConstantApply(%s, %s)", spec.getClassName(), spec.getValue().getInvocationName()));
    method.addModifiers(Modifier.PUBLIC);
    method.returns(val.getReturnType());

    method.addTypeVariables(spec.getValue().getTypeParameters());

    final String[] args = new String[val.getParams().size()];

    method.addParameter(ParameterSpec.builder(spec.getTargetTypeDecl(), "visitor").build());

    for (int i = 0; i < val.getParams().size(); ++i) {

      if (i == spec.getBindParam()) {

        method.addParameter(ParameterSpec.builder(this.ctx.getBase().getTarget(), "instance").build());
        args[i] = String.format("(%s)%s", type.getTarget(), "instance");

      } else {

        method.addParameter(ParameterSpec.builder(val.getParams().get(i).getType(), val.getParams().get(i).getName()).build());
        args[i] = val.getParams().get(i).getName();

      }

    }

    if (val.getReturnType() == TypeName.VOID) {
      method.addStatement("visitor.$L$L($L)", val.getInvocationName(), type.getName(), Joiner.on(", ").join(args));
    } else {
      method.addStatement("return visitor.$L$L($L)", val.getInvocationName(), type.getName(), Joiner.on(", ").join(args));
    }

    return method.build();
  }

  TypeName declType() {
    return this.ctx.getBase().getTarget();
  }

  public MethodSpec buildFromInstance() {
    //

    final MethodSpec.Builder method = MethodSpec.methodBuilder("fromInstance");
    method.addModifiers(Modifier.STATIC);
    method.addModifiers(Modifier.PUBLIC);
    method.returns(ClassName.bestGuess("Type"));
    method.addParameter(ParameterSpec.builder(this.declType(), "instance").build());

    method.addStatement("java.util.Objects.requireNonNull(instance)");

    final CodeBlock.Builder cb = CodeBlock.builder();

    int count = 0;

    for (final Type type : this.ctx.getImpls()) {
      if (count++ == 0) {
        cb.beginControlFlow("if (instance instanceof $L)", ModelState.rawtype(type.getTarget()));
      } else {
        cb.nextControlFlow("else if (instance instanceof $L)", ModelState.rawtype(type.getTarget()));
      }
      cb.addStatement("return $L", type.getName());
    }

    cb.endControlFlow();
    cb.addStatement("throw new RuntimeException(String.format(\"Can't map instance of '%s'\", instance.getClass().getCanonicalName()))");

    method.addCode(cb.build());

    return method.build();
  }

  public MethodSpec buildFromClass() {

    final MethodSpec.Builder method = MethodSpec.methodBuilder("fromClass");
    method.addModifiers(Modifier.STATIC);
    method.addModifiers(Modifier.PUBLIC);
    method.addTypeVariable(TypeVariableName.get("T", this.declType()));
    method.returns(ClassName.bestGuess("Type"));
    method.addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("T")), "klass").build());

    method.addStatement("java.util.Objects.requireNonNull(klass)");

    final CodeBlock.Builder cb = CodeBlock.builder();

    int count = 0;

    for (final Type type : this.ctx.getImpls()) {
      if (count++ == 0) {
        cb.beginControlFlow("if ($L.class.isAssignableFrom(klass))", ModelState.rawtype(type.getTarget()));
      } else {
        cb.nextControlFlow("else if ($L.class.isAssignableFrom(klass))", ModelState.rawtype(type.getTarget()));
      }
      cb.addStatement("return $L", type.getName());
    }

    cb.endControlFlow();
    cb.addStatement("throw new RuntimeException(String.format(\"Can't map type '%s'\", klass.getCanonicalName()))");

    method.addCode(cb.build());

    return method.build();

  }

}
