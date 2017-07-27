package io.zrz.visitors.apt;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class MirrorStuff {

  public static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
    final String clazzName = clazz.getName();
    for (final AnnotationMirror m : typeElement.getAnnotationMirrors()) {
      if (m.getAnnotationType().toString().equals(clazzName)) {
        return m;
      }
    }
    return null;
  }

  public static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
    for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals(key)) {
        return entry.getValue();
      }
    }
    return null;
  }

  public static TypeMirror getAnnotationKeyValue(TypeElement foo, Class<?> klass, String key) {
    final AnnotationMirror am = getAnnotationMirror(foo, klass);
    if (am == null) {
      return null;
    }
    final AnnotationValue av = getAnnotationValue(am, key);
    if (av == null) {
      return null;
    } else {
      return (TypeMirror) av.getValue();
    }
  }

  public static <T> Optional<T> getAnnotationKeyValue(AnnotationMirror am, String key, Class<T> klass) {
    if (am == null) {
      return Optional.empty();
    }
    final AnnotationValue av = getAnnotationValue(am, key);
    if (av == null) {
      return Optional.empty();
    } else {
      return Optional.of((T) av.getValue());
    }
  }

  public static <T> Optional<T> getAnnotationKeyValue(AnnotationMirror am, String key) {
    if (am == null) {
      return Optional.empty();
    }
    final AnnotationValue av = getAnnotationValue(am, key);
    if (av == null) {
      return Optional.empty();
    } else {
      return Optional.of((T) av.getValue());
    }
  }

  public static List<AnnotationValue> getAnnotationListKeyValue(AnnotationMirror am, String key) {
    if (am == null) {
      return Collections.emptyList();
    }
    final AnnotationValue av = getAnnotationValue(am, key);
    if (av == null) {
      return Collections.emptyList();
    } else {
      return (List<AnnotationValue>) av.getValue();
    }
  }

  public static Optional<ExecutableElement> getFunctionalInterface(Element type) {

    for (final Element e : type.getEnclosedElements()) {

      if (e instanceof ExecutableElement) {

        final ExecutableElement ex = (ExecutableElement) e;

        if (ex.isDefault()) {
          continue;
        }

        for (final Modifier modifier : ex.getModifiers()) {
          switch (modifier) {
            case ABSTRACT:
              return Optional.of(ex);
            case DEFAULT:
            case FINAL:
            case NATIVE:
            case PRIVATE:
            case PROTECTED:
            case PUBLIC:
            case STATIC:
            case STRICTFP:
            case SYNCHRONIZED:
            case TRANSIENT:
            case VOLATILE:
              break;
            default:
              break;

          }
        }

      }
    }

    return Optional.empty();
  }

  public static String getFullPackageName(PackageElement pkgelt) {
    return pkgelt.getQualifiedName().toString();
  }

}
