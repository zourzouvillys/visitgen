package io.saasy.genmod;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import io.joss.graphql.core.schema.model.InputField;
import io.joss.graphql.core.schema.model.InputType;
import io.zrz.haimod.GenContext;

public class TypeGenerator {

  private final TypeSpec.Builder container;

  public TypeGenerator(TypeSpec.Builder types) {
    this.container = types;
  }

  public void generate(GenContext ctx, InputType type) {

    final TypeSpec.Builder typeClass = TypeSpec.classBuilder(type.getName()).addModifiers(Modifier.STATIC, Modifier.PUBLIC);

    typeClass.addAnnotation(AnnotationSpec.builder(lombok.Value.class).build());
    typeClass.addAnnotation(AnnotationSpec.builder(lombok.Builder.class).build());
    typeClass.addAnnotation(AnnotationSpec.builder(lombok.experimental.Wither.class).build());

    for (final InputField field : type.getFields()) {
      final FieldSpec.Builder fieldSpec = field.getType().apply(new InputFieldGenerator(ctx, type, field, typeClass));
      fieldSpec.addModifiers(Modifier.PRIVATE);
      fieldSpec.addModifiers(Modifier.FINAL);
      typeClass.addField(fieldSpec.build());
    }

    this.container.addType(typeClass.build());

  }

}
