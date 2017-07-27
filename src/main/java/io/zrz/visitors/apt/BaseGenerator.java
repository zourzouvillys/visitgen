package io.zrz.visitors.apt;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.zrz.visitors.apt.ReflectedVisitable.Type;
import io.zrz.visitors.apt.ReflectedVisitable.Visitor;
import io.zrz.visitors.apt.ReflectedVisitable.VisitorBinder;

public class BaseGenerator {

  private final GenerationContext ctx;

  public BaseGenerator(GenerationContext ctx) {
    this.ctx = ctx;
  }

  /**
   * generates the main "Types" enum class.
   */

  TypeSpec generateTypes(GenerationContext ctx) {
    final TypeSpec.Builder enumbuilder = TypeSpec.enumBuilder("Type");
    final EnumBodyGenerator gen = new EnumBodyGenerator(ctx);
    for (final Type type : ctx.getImpls()) {
      enumbuilder.addEnumConstant(type.getName(), gen.generate(type));
    }

    // add the static enum fromInstance and fromClass methods that select a
    // type.

    enumbuilder.addMethod(gen.buildFromInstance());
    enumbuilder.addMethod(gen.buildFromClass());

    return enumbuilder.build();
  }

  /**
   *
   * @param ctx
   * @return
   */

  TypeSpec generateInvoker(GenerationContext ctx) {

    final TypeSpec.Builder iface = TypeSpec.interfaceBuilder("Invoker");

    iface.addModifiers(Modifier.STATIC);
    iface.addModifiers(Modifier.PUBLIC);

    for (final Visitor spec : ctx.getBase().getVisitors()) {

      final MethodSpec.Builder applyMethod = MethodSpec.methodBuilder(spec.getValue().getInvocationName());

      applyMethod.addJavadoc(String.format("generateBinder(%s, %s)", ctx.getBase().getClassName(), spec.getClassName()));

      applyMethod.addModifiers(Modifier.PUBLIC);
      applyMethod.addModifiers(Modifier.ABSTRACT);

      applyMethod.returns(spec.getValue().getReturnType());

      applyMethod.addTypeVariables(spec.getValue().getTypeParameters());

      final VisitorBinder val = spec.getValue();

      final String[] args = new String[val.getParams().size()];

      applyMethod.addParameter(ParameterSpec.builder(spec.getTargetTypeDecl(), "visitor").build());

      for (int i = 0; i < val.getParams().size(); ++i) {

        if (i == spec.getBindParam()) {

          applyMethod.addParameter(ParameterSpec.builder(this.ctx.getBase().getTarget(), "instance").build());

        } else {

          applyMethod.addParameter(ParameterSpec.builder(val.getParams().get(i).getType(), val.getParams().get(i).getName()).build());
          args[i] = val.getParams().get(i).getName();

        }

      }
      // applyMethod.addParameter(ParameterSpec.builder(spec,
      // "visitor").build());

      // spec.getMethodParameters().forEach(consumer ->
      // applyMethod.addParameter(consumer.apply(className)));

      iface.addMethod(applyMethod.build());

    }

    return iface.build();
  }

  private MethodSpec generateMainClassInvoker(Visitor v) {

    final MethodSpec.Builder method = MethodSpec.methodBuilder(v.getValue().getInvocationName());

    method.addModifiers(Modifier.PUBLIC);
    method.addModifiers(Modifier.STATIC);

    // method.addJavadoc(String.format("generateMainClassInvoker(%s, %s)", v.));

    // method.returns(spec.returnType());
    // method.addTypeVariables(spec.typeVariables());
    //
    // method.addParameter(ParameterSpec.builder(spec.toType(a -> a),
    // "visitor").build());
    // method.addParameter(ParameterSpec.builder(klass, "instance").build());
    //
    // final ClassName child = klass;
    //
    // method.addParameters(spec.getMethodParameters().stream()
    // .map(a -> a.apply(child))
    // .filter(a -> !a.type.equals(klass))
    // .collect(Collectors.toList()));
    //
    // final String out = spec.getMethodParameters().stream()
    // .map(a -> a.apply(child))
    // .map(p -> p.type.equals(klass) ? "instance" : p.name)
    // .collect(Collectors.joining(", "));
    //
    // method.addStatement(
    // ((spec.returnType() == TypeName.VOID) ? "" : "return ")
    // + "Type.fromInstance(instance).$L(visitor$L)",
    // spec.applyName(),
    // out.equals("") ? "" : (", " + out));

    return method.build();
  }

}
