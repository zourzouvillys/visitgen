package io.saasy.genmod;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.joss.graphql.core.schema.TypeRefVisitors;
import io.joss.graphql.core.schema.model.GenericTypeRef;
import io.joss.graphql.core.schema.model.InputCompatibleType;
import io.joss.graphql.core.schema.model.InputField;
import io.joss.graphql.core.schema.model.InputType;
import io.joss.graphql.core.schema.model.SimpleTypeRef;
import io.zrz.haimod.GenContext;

/**
 * Generates a java field for an input type.
 *
 * @author theo
 *
 */

public class InputFieldGenerator implements TypeRefVisitors.GenericTypRefReturnVisitor<InputCompatibleType, FieldSpec.Builder> {

  private final InputType type;
  private final InputField field;
  private final GenContext genctx;

  public InputFieldGenerator(GenContext genctx, InputType type, InputField field, TypeSpec.Builder typeClass) {
    this.genctx = genctx;
    this.type = type;
    this.field = field;
  }

  @Override
  public FieldSpec.Builder visitSimpleTypeRef(SimpleTypeRef<InputCompatibleType> value) {
    final TypeName javaType = this.genctx.getTypeMapper().map(value);
    return FieldSpec.builder(javaType, this.field.getName());
  }

  @Override
  public FieldSpec.Builder visitGenericTypeRef(GenericTypeRef<InputCompatibleType> value) {

    final TypeName javaType = this.genctx.getTypeMapper().map(value);
    return FieldSpec.builder(javaType, this.field.getName());
  }

}
