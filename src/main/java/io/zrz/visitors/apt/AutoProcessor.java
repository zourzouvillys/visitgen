package io.zrz.visitors.apt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

import io.zrz.visitors.VisitableUtils;
import io.zrz.visitors.annotations.Visitable;
import lombok.RequiredArgsConstructor;

@AutoService(Processor.class)
public class AutoProcessor extends AbstractProcessor {

  private final Map<String, Model> visitable = new HashMap<>();

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
    return Sets.newHashSet(Visitable.class.getCanonicalName());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

    for (final TypeElement te : annotations) {
      for (final Element e : env.getElementsAnnotatedWith(te)) {
        final TypeElement type = (TypeElement) e;
        this.visitable.computeIfAbsent(type.getQualifiedName().toString(), Model::new);
      }
    }

    for (final Element root : env.getRootElements()) {
      switch (root.getKind()) {
        case CLASS:
        case ENUM:
        case INTERFACE:
          final TypeElement type = (TypeElement) root;
          if (VisitableUtils.isVisitableType(type)) {
            for (final TypeElement base : VisitableUtils.getVisitableBase(root)) {
              this.visitable.computeIfAbsent(base.getQualifiedName().toString(), Model::new).types.add(type.getQualifiedName().toString());
            }
          }
          break;
        default:
          break;

      }
    }

    this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ROUND: " + env.getRootElements().size());

    if (env.processingOver()) {

      this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "DONE: " + this.visitable.size());

      final Builder b = new Builder(this.processingEnv.getFiler());

      this.visitable.forEach((k, v) -> {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "TYPE: " + k + ": " + v.types.stream().collect(Collectors.joining(", ")));
        b.add(k, v.types);
      });

    }
    return true;
  }

}
