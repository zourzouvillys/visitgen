package io.saasy.genmod;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.joss.graphql.core.schema.model.ObjectField;
import io.joss.graphql.core.schema.model.ObjectFieldParam;
import io.joss.graphql.core.schema.model.ObjectType;
import io.zrz.haimod.GenContext;

public class ObjectGenerator {

  private final TypeSpec.Builder container;

  public ObjectGenerator(TypeSpec.Builder types) {
    this.container = types;
  }

  public void generate(GenContext ctx, ObjectType type) {

    final TypeSpec.Builder typeClass = TypeSpec.interfaceBuilder(type.getName()).addModifiers(Modifier.STATIC, Modifier.PUBLIC);

    for (final ObjectField field : type.getFields()) {

      final TypeName javaReturnType = ctx.getTypeMapper().map(field.getFieldType());

      final MethodSpec.Builder mb = MethodSpec.methodBuilder(field.getName());

      mb.returns(javaReturnType);

      mb.addModifiers(Modifier.PUBLIC);
      mb.addModifiers(Modifier.ABSTRACT);

      for (final ObjectFieldParam param : field.getParameters()) {

        mb.addParameter(this.makeParam(ctx, type, field, param));

      }

      typeClass.addMethod(mb.build());

    }

    this.container.addType(typeClass.build());

  }

  private ParameterSpec makeParam(GenContext ctx, ObjectType type, ObjectField field, ObjectFieldParam param) {
    return ParameterSpec.builder(ctx.getTypeMapper().map(param.getType()), param.getName()).build();
  }

}
