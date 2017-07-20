package io.zrz.visitors;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.zrz.visitors.annotations.Visitable;

public class VisitableUtils {

  public static Stream<TypeElement> superTypes(TypeMirror root) {

    if (!(root instanceof DeclaredType)) {
      return Stream.empty();
    }

    final DeclaredType type = (DeclaredType) root;
    final TypeElement elt = (TypeElement) type.asElement();

    final Stream<TypeElement> parents = elt.getInterfaces().stream().flatMap(VisitableUtils::superTypes);

    return Stream.concat(Stream.of(elt), Stream.concat(superTypes(elt.getSuperclass()), parents));
  }

  public static boolean isVisitableBase(TypeElement root) {
    if (superTypes(root.asType()).filter(type -> type.getAnnotation(Visitable.Base.class) != null).findAny().isPresent()) {
      return true;
    }
    return false;
  }

  public static boolean isVisitableType(TypeElement root) {
    return root.getAnnotation(Visitable.Type.class) != null;

    // if (superTypes(root.asType()).filter(type ->
    // type.getAnnotation(Visitable.Base.class) != null).findAny().isPresent())
    // {
    // return true;
    // }
    // return false;
  }

  public static List<TypeElement> getVisitableBase(Element root) {
    return superTypes(root.asType()).filter(type -> type.getAnnotation(Visitable.Base.class) != null).collect(Collectors.toList());
  }

}
