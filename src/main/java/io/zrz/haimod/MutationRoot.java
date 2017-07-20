package io.zrz.haimod;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.joss.graphql.core.schema.model.ObjectType;
import io.joss.graphql.core.schema.model.Schema;
import io.joss.graphql.core.schema.model.Type;
import lombok.Getter;

public class MutationRoot {

  @Getter
  private final Collection<Mutation> mutations;
  private final GenContext ctx;
  @Getter
  private final ObjectType objectType;
  @Getter
  private final String name;
  @Getter
  private final Schema schema;

  MutationRoot(GenContext ctx, Schema schema, ObjectType objectType) {
    this.schema = schema;
    this.ctx = ctx;
    this.objectType = objectType;
    this.mutations = objectType.getFields().stream().map(field -> new Mutation(ctx, this, objectType, field)).collect(Collectors.toList());
    this.name = objectType.getName();
  }

  Stream<Type> getReferencedTypes() {
    return this.mutations.stream().flatMap(mutation -> mutation.getReferencedTypes());
  }

  public void apply(GenVisitor visitor) {
    this.mutations.forEach(visitor::visitMutation);
  }

}
