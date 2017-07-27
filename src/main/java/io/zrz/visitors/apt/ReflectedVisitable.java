package io.zrz.visitors.apt;

import java.util.List;

import com.squareup.javapoet.TypeName;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

public final class ReflectedVisitable {

  @Value
  @Builder
  public static class Base {
    TypeName target;
    @Singular
    List<Visitor> visitors;
    private String packageName;
    private String className;
  }

  @Value
  @Builder
  public static class Visitor {
    TypeName target;
    private VisitorBinder value;
    private int bindParam;
    private VisitorBinder bindType;
    private String className;
    private String packageName;
  }

  @Value
  @Builder
  public static class Type {
    TypeName target;
    private String name;
    private String paramName;

    // all of the base types that this type implements
    @Singular
    private List<TypeName> bases;

  }

  @Value(staticConstructor = "of")
  public static class VisitorParam {
    private String name;
    private TypeName type;
  }

  @Value
  @Builder
  public static class VisitorBinder {

    // the class we target as our functional interface
    private TypeName target;

    private TypeName returnType;

    // the method name.
    private String invocationName;

    @Singular
    private List<VisitorParam> params;

  }

}
