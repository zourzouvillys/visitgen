package io.zrz.haimod;

import io.joss.graphql.core.schema.model.ObjectType;
import io.joss.graphql.core.schema.model.Type;
import io.joss.graphql.core.schema.model.TypeRef;

public interface GenVisitor {

  void visitSchema(GenSchema schema);

  void visitMutationRoot(MutationRoot root);

  void visitMutation(Mutation mutation);

  void visitMutationParameter(MutationParameter mutation);

  void visitQueryRoot(QueryRoot root);

  void visitSubscriptionRoot(SubscriptionRoot root);

  void visitTypeRef(TypeRef<? extends Type> type);

  void visitObjectType(ObjectType objectType);

}
