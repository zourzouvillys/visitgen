package io.zrz.haimod;

import io.joss.graphql.core.schema.model.InputCompatibleType;
import io.joss.graphql.core.schema.model.ObjectFieldParam;
import io.joss.graphql.core.schema.model.TypeRef;

public class MutationParameter {

  private final Mutation mutation;
  private final ObjectFieldParam param;

  public MutationParameter(Mutation mutation, ObjectFieldParam param) {
    this.mutation = mutation;
    this.param = param;
  }

  public String getName() {
    return this.param.getName();
  }

  public TypeRef<InputCompatibleType> getTypeRef() {
    return this.param.getType();
  }

  public void apply(GenVisitor visitor) {
    visitor.visitTypeRef(this.param.getType());
  }

}
