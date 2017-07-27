package io.zrz.visitors.apt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

import io.zrz.visitors.annotations.Visitable;
import io.zrz.visitors.apt.ReflectedVisitable.Base;
import io.zrz.visitors.apt.ReflectedVisitable.Type;
import lombok.RequiredArgsConstructor;

@AutoService(Processor.class)
public class AutoProcessor extends AbstractProcessor {

  private final Map<String, Model> visitable = new HashMap<>();
  private boolean done;
  private int count = 0;

  private static ModelState state = new ModelState();

  @RequiredArgsConstructor
  private class Model {
    final String base;
    final Set<String> types = new HashSet<>();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Sets.newHashSet(Visitable.Base.class.getCanonicalName(),
        Visitable.Type.class.getCanonicalName(),
        Visitable.class.getCanonicalName(),
        Visitable.Visitor.class.getCanonicalName());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

    this.count++;

    final AnnotationScanner scanner = new AnnotationScanner(super.processingEnv);

    for (final TypeElement te : annotations) {
      for (final Element e : env.getElementsAnnotatedWith(te)) {
        final TypeElement type = (TypeElement) e;
        this.visitable.computeIfAbsent(type.getQualifiedName().toString(), Model::new);
        scanner.add(type);
      }
    }

    this.processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER,
        String.format("Round %d, %d elements, over=%s", this.count, env.getRootElements().size(), env.processingOver()));

    for (final Entry<String, Base> base : scanner.bases.entrySet()) {
      this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format("BASE: %s: %s", base.getKey(), base.getValue()));
    }

    for (final Entry<String, Type> base : scanner.types.entrySet()) {
      this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format("TYPE: %s: %s", base.getKey(), base.getValue()));
    }

    state.round(this.processingEnv, env, scanner);

    return true;

  }

}
