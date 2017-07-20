package io.saasy.genmod;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import io.zrz.haimod.GenContext;
import io.zrz.haimod.GenUtils;
import io.zrz.haimod.Mutation;
import io.zrz.haimod.MutationParameter;
import io.zrz.haimod.MutationRoot;
import io.zrz.haimod.helpers.AbstractMutationRootInterfaceGenerator;

public class ActionCreatorGenerator extends AbstractMutationRootInterfaceGenerator {

  public ActionCreatorGenerator(GenContext ctx, MutationRoot mutationRoot) {
    super(ctx, mutationRoot);
  }

  @Override
  protected String className() {
    return "ActionCreators";
  }

  @Override
  protected String comments() {
    return "provides an action which generates events once dispatched. if any async requests need to be done, they shold be done here.";
  }
  // ---

  @Override
  protected Modifier[] getMethodModifiers(Mutation mutation) {
    return new Modifier[] { Modifier.PUBLIC, Modifier.ABSTRACT };
  }

  @Override
  protected String getMethodName(Mutation mutation) {
    return "create" + GenUtils.uppercaseFirst(mutation.getName()) + "Action";
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
    return ParameterizedTypeName.get(ClassName.bestGuess("io.saasy.hai.webrtc.WebRtcStore.Action"), this.ctx.getTypeMapper().map(mutation.getReturnType()));
  }

}