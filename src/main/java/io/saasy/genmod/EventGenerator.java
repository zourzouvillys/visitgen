package io.saasy.genmod;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import io.zrz.haimod.GenContext;
import io.zrz.haimod.GenUtils;
import io.zrz.haimod.Mutation;
import io.zrz.haimod.MutationParameter;
import io.zrz.haimod.MutationRoot;
import io.zrz.haimod.helpers.AbstractMutationRootInterfaceGenerator;

public class EventGenerator extends AbstractMutationRootInterfaceGenerator {

  public EventGenerator(GenContext ctx, MutationRoot mutationRoot) {
    super(ctx, mutationRoot);
  }

  @Override
  protected String className() {
    return "Events";
  }

  // ---

  @Override
  protected Modifier[] getMethodModifiers(Mutation mutation) {
    return new Modifier[] { Modifier.PUBLIC, Modifier.ABSTRACT };
  }

  @Override
  protected String getMethodName(Mutation mutation) {
    return GenUtils.lowercaseFirst(mutation.getEventName());
  }

  @Override
  protected Iterable<ParameterSpec> buildParameters(Mutation mutation) {
    final List<ParameterSpec> params = new LinkedList<>();
    for (final MutationParameter param : mutation.getParameters()) {
      params.add(ParameterSpec.builder(this.ctx.getTypeMapper().map(param.getTypeRef()), param.getName()).build());
    }
    return params;
  }

  @Override
  protected TypeName calculateReturnType(Mutation mutation) {
    return this.ctx.getTypeMapper().map(mutation.getReturnType());
  }

}