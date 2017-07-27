package io.zrz.visitors.apt;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

import io.zrz.visitors.apt.ReflectedVisitable.Type;
import io.zrz.visitors.apt.ReflectedVisitable.Visitor;
import io.zrz.visitors.apt.ReflectedVisitable.VisitorBinder;
import io.zrz.visitors.apt.ReflectedVisitable.VisitorParam;

/**
 * generates a visitor interface.
 */

public class VisitorWriter {

  public VisitorWriter(GenerationContext ctx) {
  }

  public Collection<TypeSpec> generate(GenerationContext ctx, Visitor visitor) {

    final Builder tb = TypeSpec.interfaceBuilder(visitor.getClassName());

    tb.addModifiers(Modifier.STATIC);
    tb.addModifiers(Modifier.PUBLIC);

    final VisitorBinder val = visitor.getValue();

    // add the type parameters

    tb.addTypeVariables(val.getTypeParameters());

    for (final Type type : ctx.getImpls()) {
      tb.addMethod(
          MethodSpec.methodBuilder(val.getInvocationName() + type.getName())
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addParameters(this.params(visitor, type))
              .returns(val.getReturnType())
              .build());

    }

    return Arrays.asList(tb.build());

  }

  private Collection<ParameterSpec> params(Visitor visitor, Type type) {

    // first arg is the item.

    final List<ParameterSpec> args = new LinkedList<>();

    final VisitorBinder val = visitor.getValue();

    for (int i = 0; i < val.getParams().size(); ++i) {

      final VisitorParam p = val.getParams().get(i);

      if (i == visitor.getBindParam()) {

        args.add(ParameterSpec.builder(type.getTarget(), "arg").build());

      } else {

        args.add(ParameterSpec.builder(p.getType(), p.getName()).build());

      }

    }

    return args;

  }

}
