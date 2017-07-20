package io.zrz.haimod;

import java.util.function.Predicate;

import io.joss.graphql.core.schema.model.Type;

public class GenPredicates {

  public static Predicate<Type> hasDirective(String name) {
    return type -> type.hasDirective(name);
  }

  public static Predicate<Mutation> withDirective(String name) {
    return m -> m.hasDirective(name);
  }

}
