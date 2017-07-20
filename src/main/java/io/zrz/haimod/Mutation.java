package io.zrz.haimod;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import io.joss.graphql.core.schema.model.ObjectField;
import io.joss.graphql.core.schema.model.ObjectType;
import io.joss.graphql.core.schema.model.Type;
import io.joss.graphql.core.schema.model.TypeRef;
import io.joss.graphql.core.value.GQLValueConverters;

public class Mutation {

  private final GenContext ctx;
  private final ObjectType type;
  private final ObjectField field;
  private final List<MutationParameter> params;
  private final TypeRef<Type> returnType;
  private final MutationRoot mutationRoot;

  public Mutation(GenContext ctx, MutationRoot mutationRoot, ObjectType type, ObjectField field) {
    this.ctx = ctx;
    this.mutationRoot = mutationRoot;
    this.type = type;
    this.field = field;
    this.returnType = field.getFieldType();
    this.params = field.getParameters().stream().map(param -> new MutationParameter(this, param)).collect(Collectors.toList());
  }

  public Collection<MutationParameter> getParameters() {
    return this.params;
  }

  public String getEventName() {
    return this.field.getDirective("autoEvent")
        .flatMap(in -> in.arg("type"))
        .map(in -> in.value())
        .map(in -> in.apply(GQLValueConverters.stringConverter()))
        .orElse(null);
  }

  public String getName() {
    return this.field.getName();
  }

  public TypeRef<Type> getReturnType() {
    return this.returnType;
  }

  public Stream<Type> getReferencedTypes() {
    return Stream.concat(this.getParameters().stream().map(p -> p.getTypeRef().getRawType()), Lists.newArrayList(this.returnType.getRawType()).stream());
  }

  public void apply(GenVisitor visitor) {

    visitor.visitTypeRef(this.returnType);
    this.params.forEach(visitor::visitMutationParameter);

  }

  public boolean hasDirective(String name) {
    return this.field.hasDirective(name);
  }

}
