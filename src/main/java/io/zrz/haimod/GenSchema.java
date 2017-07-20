package io.zrz.haimod;

import java.util.Optional;

import io.joss.graphql.core.schema.model.Model;
import io.joss.graphql.core.schema.model.ObjectType;
import io.joss.graphql.core.schema.model.Schema;
import io.joss.graphql.core.schema.model.Type;
import io.joss.graphql.core.value.GQLValueConverters;
import lombok.Getter;

public class GenSchema {

  @Getter
  private final GenContext context;
  @Getter
  private final Schema schema;
  @Getter
  private MutationRoot mutationRoot;
  @Getter
  private QueryRoot queryRoot;
  @Getter
  private SubscriptionRoot subscriptionRoot;

  public GenSchema(GenContext context, Model model, Schema schema) {

    this.context = context;
    this.schema = schema;

    if (schema.hasKey(Schema.MUTATION)) {
      final ObjectType objectType = model.getObjectType(schema.value(Schema.MUTATION));
      this.mutationRoot = new MutationRoot(context, schema, objectType);
    }

    if (schema.hasKey(Schema.QUERY)) {
      final ObjectType objectType = model.getObjectType(schema.value(Schema.QUERY));
      this.queryRoot = new QueryRoot(context, schema, objectType);
    }

    if (schema.hasKey(Schema.SUBSCRIPTION)) {
      final ObjectType objectType = model.getObjectType(schema.value(Schema.SUBSCRIPTION));
      this.subscriptionRoot = new SubscriptionRoot(context, schema, objectType);
    }

  }

  public Type getUnknownRoot(String name) {
    return this.context.getModel().getInputType(name);
  }

  public Optional<String> getPackage() {
    return this.schema.getDirective("java")
        .map(d -> d.arg("package")
            .map(arg -> arg.value().apply(GQLValueConverters.stringConverter())).orElse(null));
  }

  public void apply(GenVisitor visitor) {
    if (this.mutationRoot != null) {
      visitor.visitMutationRoot(this.mutationRoot);
    }
    if (this.queryRoot != null) {
      visitor.visitQueryRoot(this.queryRoot);
    }
    if (this.subscriptionRoot != null) {
      visitor.visitSubscriptionRoot(this.subscriptionRoot);
    }
  }

}
