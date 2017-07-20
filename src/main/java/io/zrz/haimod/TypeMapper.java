package io.zrz.haimod;

import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import io.joss.graphql.core.doc.GQLDirective;
import io.joss.graphql.core.schema.TypeRefVisitors.GenericTypRefReturnVisitor;
import io.joss.graphql.core.schema.model.GenericTypeRef;
import io.joss.graphql.core.schema.model.InputType;
import io.joss.graphql.core.schema.model.ObjectType;
import io.joss.graphql.core.schema.model.SimpleTypeRef;
import io.joss.graphql.core.schema.model.Type;
import io.joss.graphql.core.schema.model.TypeRef;
import io.joss.graphql.core.value.GQLListValue;
import io.joss.graphql.core.value.GQLStringValue;
import io.joss.graphql.core.value.GQLValue;
import io.joss.graphql.core.value.GQLValueConverters;
import io.zrz.haimod.api.ListValueNotNull;
import lombok.SneakyThrows;

public class TypeMapper {

  public <T extends Type> TypeName map(TypeRef<T> value) {

    return value.apply(new GenericTypRefReturnVisitor<T, TypeName>() {

      @Override
      public TypeName visitSimpleTypeRef(SimpleTypeRef<T> value) {
        return TypeMapper.this.map(value);
      }

      @Override
      public TypeName visitGenericTypeRef(GenericTypeRef<T> value) {

        final TypeRef<T> innerRef = value.getTypeRef();
        TypeName type = innerRef.apply(this);

        // eclipse JDT bug: annotation on generic type params with qualified
        // namespace ("java.lang.String") doesn't work. Use our own annotatio
        // nfor now.
        type = type.withoutAnnotations();

        if (type.isPrimitive()) {
          type = box(type);
        }

        TypeName javaType = ParameterizedTypeName.get(ClassName.get(Collection.class), type);

        if (!innerRef.isNullable()) {
          javaType = javaType.annotated(AnnotationSpec.builder(ListValueNotNull.class).build());
        }

        if (!value.isNullable()) {
          return javaType.annotated(AnnotationSpec.builder(NotNull.class).build());
        }
        return javaType;
      }

    });

  }

  static TypeName box(TypeName name) {
    if (name.equals(TypeName.BOOLEAN)) {
      return TypeName.BOOLEAN.box();
    }
    if (name.equals(TypeName.BYTE)) {
      return TypeName.BYTE.box();
    }
    if (name.equals(TypeName.SHORT)) {
      return TypeName.SHORT.box();
    }
    if (name.equals(TypeName.INT)) {
      return TypeName.INT.box();
    }
    if (name.equals(TypeName.LONG)) {
      return TypeName.LONG.box();
    }
    if (name.equals(TypeName.CHAR)) {
      return TypeName.CHAR.box();
    }
    if (name.equals(TypeName.FLOAT)) {
      return TypeName.FLOAT.box();
    }
    if (name.equals(TypeName.DOUBLE)) {
      return TypeName.DOUBLE.box();
    }
    throw new AssertionError(name);
  }

  @SneakyThrows
  public <T extends Type> TypeName map(SimpleTypeRef<T> value) {

    final T type = value.getType();

    for (final GQLDirective javaTypeDecl : type.getDirectives("javaType")) {

      final GQLValue typeName = this.getJavaType(javaTypeDecl, value.isNullable());

      if (typeName != null) {

        final TypeName javaType = this.fromString(typeName);

        if (!value.isNullable() && !javaType.isPrimitive()) {
          return javaType
              .annotated(AnnotationSpec.builder(NotNull.class).build());
        } else {
          return javaType;
        }

      }

    }

    if (type instanceof InputType) {

      return ClassName.bestGuess("Types." + type.getName());

    } else if (type instanceof ObjectType) {

      return ClassName.bestGuess("Objects." + type.getName());

    }

    throw new RuntimeException(String.format("Don't know how to map '%s' to java type", type.getName()));

  }

  TypeName fromString(GQLValue typeName) {

    // .map(x -> x.value().apply(GQLValueConverters.stringConverter()))

    if (typeName instanceof GQLStringValue) {

      final String value = typeName.apply(GQLValueConverters.stringConverter());

      switch (value) {
        case "boolean":
          return TypeName.BOOLEAN;
        case "int":
          return TypeName.INT;
        case "long":
          return TypeName.LONG;
        case "double":
          return TypeName.DOUBLE;
        case "float":
          return TypeName.FLOAT;
      }

      return ClassName.bestGuess(value);

    } else if (typeName instanceof GQLListValue) {

      final List<GQLValue> values = ((GQLListValue) typeName).values();
      final String packageName = values.get(0).apply(GQLValueConverters.stringConverter());
      return ClassName.get(packageName, ((GQLStringValue) values.get(1)).value());

    } else {
      throw new RuntimeException("Unknown value for @javaType");
    }

  }

  GQLValue getJavaType(GQLDirective directive, boolean nullable) {
    return directive.arg(nullable ? "nullable" : "nonnull").map(in -> in.value()).orElse(directive.arg("type").map(in -> in.value()).orElse(null));
  }

}
