package io.zrz.haimod;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import io.joss.graphql.core.schema.model.Model;
import io.joss.graphql.core.schema.model.Type;
import lombok.Getter;
import lombok.SneakyThrows;

public class GenContext {

  @Getter
  private final Model model;
  @Getter
  private final TypeMapper typeMapper;

  @Getter
  private final List<GenSchema> schemas;

  private final String packageName;

  private final List<Map.Entry<TypeSpec, String>> out = new LinkedList<>();

  @Getter
  private final Set<Type> referencedTypes;

  public GenContext(Model model, String packageName) {

    this.model = model;
    this.typeMapper = new TypeMapper();
    this.packageName = packageName;
    this.schemas = model.getSchemas().stream().map(schema -> new GenSchema(this, model, schema)).collect(Collectors.toList());

    // find all referenced types
    final TypeFinder collector = new TypeFinder();
    this.apply(collector);
    this.referencedTypes = collector.getTypes();

  }

  public void add(String packageName, TypeSpec type) {
    Objects.requireNonNull(packageName);
    this.out.add(Maps.immutableEntry(type, packageName));
  }

  @SneakyThrows
  public void writeTo(Path base) {
    for (final Map.Entry<TypeSpec, String> type : this.out) {
      final JavaFile.Builder file = JavaFile.builder(type.getValue(), type.getKey());
      file.build().writeTo(base);
    }
  }

  public void apply(GenVisitor visitor) {
    this.schemas.forEach(visitor::visitSchema);
  }

}
