package io.zrz.visitors.apt;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Wither;

public final class ReflectedVisitable {

  @Value
  @Builder
  @Wither
  public static class Base {
    TypeElement element;
    AnnotationMirror annotation;
    TypeName target;
    @Singular
    List<Visitor> visitors;

    // the requested name for the type related files.
    ClassName className;

  }

  @Value
  @Builder
  @Wither
  public static class Visitor {

    /**
     * the @Visitable.Visitor annotation which declared this.
     */

    AnnotationMirror annotation;

    /**
     * The type decl for this visitor's class.
     */

    TypeName target;

    /**
     * The
     */

    private VisitorBinder value;

    /**
     * the class type of the implemented visitor.
     */

    private ClassName className;

    /**
     * the parameter number which the actual instance is passed in as.
     */

    private int bindParam;

    /**
     * optional binding type. provides the ability to bind an instance and
     * return a lambda.
     */

    private VisitorBinder bindType;

    /**
     * The decl type for the visitor interface.
     */

    public TypeName getTargetTypeDecl() {
      return ParameterizedTypeName.get(this.className, this.value.typeParameters.toArray(new TypeName[0]));
    }
  }

  @Value
  @Builder
  @Wither
  public static class Type {

    TypeElement element;
    AnnotationMirror annotation;
    TypeName target;
    private String name;
    private String paramName;

    // all of the base types that this type implements which are marked with (or
    // supertype of) @Visitable.Base
    @Singular
    private List<TypeName> bases;

  }

  @Wither
  @Value(staticConstructor = "of")
  public static class VisitorParam {
    private String name;
    private TypeName type;
  }

  /**
   * these are the methods invoked with an instance, and dispatch to the right
   * visitor.
   */

  @Value
  @Builder
  @Wither
  public static class VisitorBinder {

    /**
     * the type of the interface which defined this binder.
     */

    private TypeName target;

    /**
     * The return type from the dispatch method.
     *
     * If this is a type variable, then it's hauled up to the class.
     *
     */

    private TypeName returnType;

    /**
     * The name of the method that dispatches. "apply", "convert", etc.
     */

    private String invocationName;

    /**
     * The params that the dispatch method takes.
     */

    @Singular
    private List<VisitorParam> params;

    /**
     * the type variables which apply to this invocation.
     */

    @Singular
    private List<TypeVariableName> typeParameters;

  }

}
