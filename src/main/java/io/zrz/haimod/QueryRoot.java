package io.zrz.haimod;

import io.joss.graphql.core.schema.model.ObjectType;
import io.joss.graphql.core.schema.model.Schema;
import lombok.Getter;

public class QueryRoot {

  private final GenContext ctx;
  @Getter
  private final ObjectType objectType;
  @Getter
  private final String name;
  @Getter
  private final Schema schema;

  QueryRoot(GenContext ctx, Schema schema, ObjectType objectType) {
    this.schema = schema;
    this.ctx = ctx;
    this.objectType = objectType;
    this.name = objectType.getName();
  }

  public void apply(GenVisitor visitor) {
    visitor.visitObjectType(this.objectType);
  }

}
