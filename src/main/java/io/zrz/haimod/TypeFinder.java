package io.zrz.haimod;

import java.util.HashSet;
import java.util.Set;

import io.joss.graphql.core.schema.model.InputField;
import io.joss.graphql.core.schema.model.InputType;
import io.joss.graphql.core.schema.model.ObjectField;
import io.joss.graphql.core.schema.model.ObjectFieldParam;
import io.joss.graphql.core.schema.model.ObjectType;
import io.joss.graphql.core.schema.model.ScalarType;
import io.joss.graphql.core.schema.model.Type;
import io.joss.graphql.core.schema.model.TypeRef;
import lombok.Getter;

/**
 * all referenced types
 */

public class TypeFinder implements GenVisitor {

  @Getter
  private final Set<Type> types = new HashSet<>();

  @Override
  public void visitSchema(GenSchema schema) {
    schema.apply(this);
  }

  @Override
  public void visitMutationRoot(MutationRoot root) {
    root.apply(this);
  }

  @Override
  public void visitMutation(Mutation mutation) {
    mutation.apply(this);

  }

  @Override
  public void visitMutationParameter(MutationParameter param) {
    param.apply(this);

  }

  @Override
  public void visitQueryRoot(QueryRoot root) {

    if (root.getObjectType() != null) {
      this.visitObjectType(root.getObjectType());
    }

  }

  @Override
  public void visitSubscriptionRoot(SubscriptionRoot root) {

  }

  @Override
  public void visitTypeRef(TypeRef<? extends Type> type) {
    this.visitRawTypeRef(type.getRawType());
  }

  private void visitRawTypeRef(Type rawType) {

    if (this.types.add(rawType)) {

      if (rawType instanceof InputType) {
        this.visitInputType((InputType) rawType);
      } else if (rawType instanceof ObjectType) {
        this.visitObjectType((ObjectType) rawType);
      } else if (rawType instanceof ScalarType) {
        //
      } else {
        throw new RuntimeException(rawType.getClass().toString());
      }

    }

  }

  private void visitInputType(InputType inputType) {
    inputType.getFields().forEach(this::visitInputField);
  }

  @Override
  public void visitObjectType(ObjectType objectType) {
    objectType.getFields().forEach(this::visitObjectField);
  }

  private void visitInputField(InputField inputField) {
    this.visitTypeRef(inputField.getFieldType());
  }

  private void visitObjectField(ObjectField objectField) {
    this.visitTypeRef(objectField.getFieldType());
    objectField.getFieldArgs().forEach(this::visitObjectFieldParam);
  }

  private void visitObjectFieldParam(ObjectFieldParam param) {
    this.visitTypeRef(param.getType());
  }

}
