package io.saasy.genmod;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import io.zrz.haimod.GenContext;
import io.zrz.haimod.Mutation;
import io.zrz.haimod.MutationParameter;
import io.zrz.haimod.MutationRoot;
import io.zrz.haimod.helpers.AbstractMutationRootInterfaceGenerator;

public class MiddlewareGenerator extends AbstractMutationRootInterfaceGenerator {

  public MiddlewareGenerator(GenContext ctx, MutationRoot mutationRoot) {
    super(ctx, mutationRoot);
  }

  @Override
  protected String className() {
    return "Middleware";
  }

  // ---

  @Override
  protected Modifier[] getMethodModifiers(Mutation mutation) {
    return new Modifier[] { Modifier.PUBLIC, Modifier.ABSTRACT };
  }

  @Override
  protected String comments() {
    return "Receive an action, dispatch or block events.";
  }

  @Override
  protected void configure(Mutation mutation, MethodSpec.Builder method) {

    // method.addStatement("return dispatcher.dispatch(new $L())",
    // mutation.getEventName());

  }

  @Override
  protected String getMethodName(Mutation mutation) {
    return mutation.getName();
  }

  @Override
  protected Iterable<ParameterSpec> buildParameters(Mutation mutation) {
    final List<ParameterSpec> params = new LinkedList<>();
    params.add(ParameterSpec.builder(this.dispatcherType(), "dispatcher").build());
    for (final MutationParameter param : mutation.getParameters()) {
      params.add(ParameterSpec.builder(this.ctx.getTypeMapper().map(param.getTypeRef()), param.getName()).build());
    }
    return params;
  }

  private TypeName dispatcherType() {
    return ClassName.get("io.saasy.hai.webrtc", "WebRtcStore", "Dispatcher");
    // return ClassName.bestGuess(".Dispatcher");
  }

  @Override
  protected TypeName calculateReturnType(Mutation mutation) {
    return this.ctx.getTypeMapper().map(mutation.getReturnType());
  }

}