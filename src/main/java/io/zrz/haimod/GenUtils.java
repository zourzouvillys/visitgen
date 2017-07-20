package io.zrz.haimod;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GenUtils {

  public static String uppercaseFirst(String name) {
    if (name == null) {
      return null;
    }
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  public static String lowercaseFirst(String name) {
    if (name == null) {
      return null;
    }
    return name.substring(0, 1).toLowerCase() + name.substring(1);
  }

  /**
   *
   * @param schemas
   * @param predicate
   * @return
   */

  public static Stream<Mutation> mutationsMatching(List<GenSchema> schemas, Predicate<Mutation> predicate) {
    return schemas.stream().flatMap(schema -> schema.getMutationRoot().getMutations().stream().filter(predicate));
  }

}
