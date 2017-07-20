package io.zrz.haimod.helpers;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.zrz.haimod.GenContext;
import io.zrz.haimod.Mutation;
import io.zrz.haimod.MutationRoot;

public abstract class AbstractMutationRootInterfaceGenerator {

  protected final GenContext ctx;
  protected final MutationRoot mutationRoot;

  public AbstractMutationRootInterfaceGenerator(GenContext ctx, MutationRoot mutationRoot) {
    this.ctx = ctx;
    this.mutationRoot = mutationRoot;
  }

  public void generate(TypeSpec.Builder container) {

    final TypeSpec.Builder mutationCalls = TypeSpec.interfaceBuilder(this.className())
        .addModifiers(Modifier.PUBLIC);

    final String comments = this.comments();
    if (comments != null) {
      mutationCalls.addJavadoc(CodeBlock.of(comments + "\n"));
    }

    this.configure(mutationCalls);

    for (final Mutation mutation : this.mutationRoot.getMutations()) {
      final MethodSpec mb = this.generate(mutation);
      if (mb != null) {
        mutationCalls.addMethod(mb);
      }
    }

    container.addType(mutationCalls.build());

  }

  protected String comments() {
    return null;
  }

  protected void configure(TypeSpec.Builder builder) {
  }

  protected void configure(Mutation mutation, MethodSpec.Builder method) {
  }

  protected abstract String className();

  public MethodSpec generate(Mutation mutation) {

    final String methodName = this.getMethodName(mutation);

    if (methodName == null) {
      return null;
    }

    final MethodSpec.Builder mb = MethodSpec.methodBuilder(methodName)
        .addModifiers(this.getMethodModifiers(mutation))
        .addParameters(this.buildParameters(mutation))
        .returns(this.calculateReturnType(mutation));

    this.configure(mutation, mb);

    return mb.build();
  }

  protected abstract TypeName calculateReturnType(Mutation mutation);

  protected abstract Iterable<ParameterSpec> buildParameters(Mutation mutation);

  protected abstract Modifier[] getMethodModifiers(Mutation mutation);

  protected abstract String getMethodName(Mutation mutation);

}
