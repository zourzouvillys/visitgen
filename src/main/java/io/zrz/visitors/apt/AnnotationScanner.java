package io.zrz.visitors.apt;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import io.zrz.visitors.annotations.Visitable;
import io.zrz.visitors.annotations.Visitable.Visitor;
import io.zrz.visitors.annotations.Visitable.Visitors;
import io.zrz.visitors.apt.ReflectedVisitable.Visitor.VisitorBuilder;
import io.zrz.visitors.apt.ReflectedVisitable.VisitorBinder;
import io.zrz.visitors.apt.ReflectedVisitable.VisitorBinder.VisitorBinderBuilder;
import io.zrz.visitors.apt.ReflectedVisitable.VisitorParam;

/**
 * Reads annotations and returns a version that is easy to work with.
 */

public class AnnotationScanner {

  private final ProcessingEnvironment env;
  final Map<String, ReflectedVisitable.Base> bases = new HashMap<>();
  final Map<String, ReflectedVisitable.Type> types = new HashMap<>();

  public AnnotationScanner(ProcessingEnvironment env) {
    this.env = Objects.requireNonNull(env);
  }

  /**
   * add a type which has been annotated with @Visitable.Type or @Visitable.Base
   * (or both).
   */

  public void add(TypeElement e) {

    final List<ReflectedVisitable.Visitor> visitors = new LinkedList<>();

    final Visitable.Visitor visitor = e.getAnnotation(Visitable.Visitor.class);

    if (visitor != null) {
      visitors.add(this.addVisitor(e, visitor));
    }

    final Visitable.Visitors bvisitors = e.getAnnotation(Visitable.Visitors.class);

    if (bvisitors != null) {
      visitors.addAll(this.addVisitors(e, bvisitors));
    }

    final Visitable.Base baseant = e.getAnnotation(Visitable.Base.class);

    if (baseant != null) {
      this.addBase(e, baseant, visitors);
    }

    final Visitable.Type type = e.getAnnotation(Visitable.Type.class);

    if (type != null) {
      this.addType(e, type);
    }

  }

  private List<io.zrz.visitors.apt.ReflectedVisitable.Visitor> addVisitors(TypeElement e, Visitors avisitors) {
    final AnnotationMirror mirror = this.getMirror(e, avisitors);
    final List<ReflectedVisitable.Visitor> visitors = new LinkedList<>();
    for (final AnnotationValue vant : MirrorStuff.getAnnotationListKeyValue(mirror, "value")) {
      final AnnotationMirror visitor = vant.accept(new AnnotationValueVisitor(), null);
      visitors.add(this.addVisitor(e, visitor));
    }
    return visitors;
  }

  List<ReflectedVisitable.Visitor> addVisitors(TypeElement e, AnnotationMirror mirror) {
    final List<ReflectedVisitable.Visitor> visitors = new LinkedList<>();
    for (final AnnotationValue vant : MirrorStuff.getAnnotationListKeyValue(mirror, "value")) {
      // the "value" element, which is a Visitable.Visitor annotation instance.
      final AnnotationMirror visitor = vant.accept(new AnnotationValueVisitor(), null);
      visitors.add(this.addVisitor(e, visitor));
    }
    return visitors;
  }

  private io.zrz.visitors.apt.ReflectedVisitable.Visitor addVisitor(TypeElement e, Visitor visitor) {
    return this.addVisitor(e, this.getMirror(e, visitor));
  }

  private ReflectedVisitable.Visitor addVisitor(TypeElement e, AnnotationMirror visitor) {

    final VisitorBuilder vb = ReflectedVisitable.Visitor.builder();

    vb.annotation(visitor);

    vb.target(this.makeType(e));

    final int bindParam = getAnnotationValue(visitor, "bindParam")
        .flatMap(aval -> Optional.ofNullable(aval.accept(new IntValueVisitor(), null)))
        .orElse(0);

    vb.bindParam(bindParam);

    // we have to ead the name ..

    final VisitorBinder binder = getAnnotationValue(visitor, "value")
        .map(val -> val.accept(new TypeNameValueVisitor(), null))
        .map(t -> this.shape(t, bindParam))
        .orElse(null);

    vb.value(binder);

    vb.bindType(getAnnotationValue(visitor, "bindType")
        .map(val -> val.accept(new TypeNameValueVisitor(), null))
        .map(t -> this.shape(t, bindParam))
        .orElse(null));

    final String pn = getAnnotationValue(visitor, "packageName")
        .map(aval -> aval.accept(new StringValueVisitor(), null))
        .orElse(MoreElements.getPackage(e).getQualifiedName().toString());

    final String cn = getAnnotationValue(visitor, "className")
        .map(aval -> aval.accept(new StringValueVisitor(), null))
        .orElse(e.getSimpleName().toString() + ModelState.rawtype(binder.getTarget()).simpleName() + "Visitor");

    vb.className(ClassName.get(pn, cn));

    return vb.build();

  }

  /**
   * scan the base annotation and build the info about it.
   *
   * @param e
   * @return
   */

  private void addBase(TypeElement e, Visitable.Base base, List<ReflectedVisitable.Visitor> visitors) {

    final ReflectedVisitable.Base.BaseBuilder b = ReflectedVisitable.Base.builder();

    b.element(e);
    b.target(this.makeType(e));

    final String[] parts = base.className().length == 0 ? new String[] { e.getSimpleName().toString() + "Visitors" } : base.className();

    final ClassName className = ClassName.get(
        AptUtils.defaultString(base.packageName(), MoreElements.getPackage(e).getQualifiedName().toString()),
        parts[0],
        parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length - 1) : new String[0]);

    b.className(className);

    final AnnotationMirror mirror = this.getMirror(e, base);

    b.annotation(mirror);

    for (final AnnotationValue vant : MirrorStuff.getAnnotationListKeyValue(mirror, "value")) {
      // the "value" element, which is a Visitable.Visitor annotation instance.
      final AnnotationMirror visitor = vant.accept(new AnnotationValueVisitor(), null);
      visitors.add(this.addVisitor(e, visitor));
    }

    b.visitors(visitors);

    this.bases.put(e.getQualifiedName().toString(), b.build());

  }

  private void addType(TypeElement e, Visitable.Type type) {
    final ReflectedVisitable.Type.TypeBuilder b = ReflectedVisitable.Type.builder();
    b.element(e);
    b.annotation(this.getMirror(e, type));
    b.target(this.makeType(e));
    b.name(AptUtils.defaultString(type.value(), e.getSimpleName().toString()));
    b.paramName(type.paramName());
    b.bases(this.superbases(e));
    this.types.put(e.getQualifiedName().toString(), b.build());
  }

  private AnnotationMirror getMirror(TypeElement e, Annotation base) {
    final List<? extends AnnotationMirror> mirrors = this.env.getElementUtils().getAllAnnotationMirrors(e);
    for (final AnnotationMirror ant : mirrors) {
      if (ant.getAnnotationType().toString().equals(base.annotationType().getCanonicalName())) {
        return ant;
      }
    }
    return null;
  }

  public static Optional<AnnotationValue> getAnnotationValue(AnnotationMirror annotationMirror, String key) {
    for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals(key)) {
        if (entry.getValue() == null) {
          return Optional.empty();
        }
        return Optional.ofNullable(entry.getValue());
      }
    }
    return Optional.empty();
  }

  private TypeName makeType(TypeElement e) {
    return TypeName.get(e.asType());
  }

  private static class TypeNameValueVisitor extends SimpleAnnotationValueVisitor8<ClassName, Void> {

    @Override
    public ClassName visitType(TypeMirror t, Void p) {
      // will always be a raw class, can't have parameterized types in
      // annotations.
      return (ClassName) TypeName.get(t);
    }

  }

  private static class AnnotationValueVisitor extends SimpleAnnotationValueVisitor8<AnnotationMirror, Void> {

    @Override
    public AnnotationMirror visitAnnotation(AnnotationMirror a, Void p) {
      return a;
    }

  }

  private static class StringValueVisitor extends SimpleAnnotationValueVisitor8<String, Void> {

    @Override
    public String visitString(String a, Void p) {
      return a;
    }

  }

  private static class ListValueVisitor extends SimpleAnnotationValueVisitor8<String[], Void> {

    @Override
    public String[] visitArray(List<? extends AnnotationValue> vals, Void p) {
      return vals.stream().map(a -> a.accept(new StringValueVisitor(), p)).toArray(String[]::new);
    }

  }

  private static class IntValueVisitor extends SimpleAnnotationValueVisitor8<Integer, Void> {

    @Override
    protected Integer defaultAction(Object o, Void p) {
      throw new IllegalArgumentException();
    }

    @Override
    public Integer visitInt(int vals, Void p) {
      return vals;
    }
  }

  /**
   * extract a binder shape from a class (which must be an interface with single
   * method).
   */

  public ReflectedVisitable.VisitorBinder shape(ClassName iface, int bindParam) {

    // given the raw name
    final TypeElement ifaceType = this.env.getElementUtils().getTypeElement(iface.reflectionName());

    // the method
    final ExecutableElement method = MirrorStuff.getFunctionalInterface(ifaceType).orElse(null);

    // the return type.
    final TypeName returnType = TypeName.get(method.getReturnType());

    final VisitorBinderBuilder b = ReflectedVisitable.VisitorBinder.builder();

    b.target(TypeName.get(ifaceType.asType()));
    b.invocationName(method.getSimpleName().toString());
    b.returnType(returnType);

    final TypeName[] vartypes = new TypeName[method.getParameters().size()];

    // calculate parameters.
    for (int i = 0; i < method.getParameters().size(); ++i) {

      final VariableElement param = method.getParameters().get(i);

      if (i == bindParam) {

        b.param(VisitorParam.of(param.getSimpleName().toString(), null));
        vartypes[i] = null;

      } else {

        final TypeName paramtype = TypeName.get(param.asType());

        vartypes[i] = paramtype;

        if (paramtype instanceof TypeVariableName) {
          vartypes[i] = paramtype;
          b.typeParameter((TypeVariableName) paramtype);
        }

        b.param(VisitorParam.of(param.getSimpleName().toString(), paramtype));

      }

    }

    if (returnType instanceof TypeVariableName) {
      b.typeParameter((TypeVariableName) returnType);
    }

    return b.build();

  }

  /**
   * gets a complete heirachy of (unresolved) interfaces and supertypes.
   */

  Set<TypeName> superbases(TypeElement type) {

    final Set<TypeName> capture = new HashSet<>();

    final TypeMirror superclass = type.getSuperclass();

    if (superclass != null) {
      this.superbases(type, capture, superclass);
    }

    type.getInterfaces().forEach(mirror -> this.superbases(type, capture, mirror));

    return capture;

  }

  /**
   * scans down, adding into the collector if this type of any of its children
   * have @Visitable.Base.
   *
   * @param type2
   *
   * @param supertypes
   * @param type
   * @return
   */

  boolean superbases(TypeElement telt, Set<TypeName> capture, TypeMirror type) {

    if (type.getKind() == TypeKind.NONE) {
      return false;
    }

    // our own type.
    final DeclaredType itype = MoreTypes.asDeclared(type);

    final DeclaredType dtype = this.env.getTypeUtils()
        .getDeclaredType(MoreElements.asType(itype.asElement()), itype.getTypeArguments().toArray(new TypeMirror[0]));

    //
    boolean found = false;

    if (dtype.asElement().getAnnotation(Visitable.Base.class) != null) {
      found = true;
      capture.add(TypeName.get(dtype));
    }

    for (final TypeMirror supertype : this.env.getTypeUtils().directSupertypes(type)) {
      if (this.superbases(MoreElements.asType(dtype.asElement()), capture, supertype)) {
        found = true;
      }
    }

    return found;

  }

}
