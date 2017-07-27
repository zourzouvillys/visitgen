package io.zrz.visitors.apt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * Keeps track of the model so we can hack around incremental compilation in
 * eclipse.
 *
 * @author theo
 *
 */

public class ModelState {

  private final Set<String> generated = new HashSet<>();
  private boolean done = false;

  final Map<String, ReflectedVisitable.Base> bases = new HashMap<>();
  final Map<String, ReflectedVisitable.Type> types = new HashMap<>();

  public void round(ProcessingEnvironment processingEnv, RoundEnvironment env, AnnotationScanner scanner) {

    if (this.done) {
      // it's a new entry into the processor.
      this.done = false;
    }

    this.bases.putAll(scanner.bases);
    this.types.putAll(scanner.types);

    if (this.shouldGenerate(env.processingOver(), env.getRootElements().size(), this.bases.isEmpty() && this.types.isEmpty())) {
      this.generate(processingEnv);
    }

    if (env.processingOver()) {
      // we're done.
      this.done = true;
    }

  }

  /**
   * calculates the models, and generates the code.
   */

  private void generate(ProcessingEnvironment env) {

    for (final ReflectedVisitable.Base base : this.bases.values()) {

      final Set<ReflectedVisitable.Type> impls = new HashSet<>();

      // find the implementing types.
      for (final ReflectedVisitable.Type type : this.types.values()) {
        if (type.getBases().stream().anyMatch(t -> ModelState.rawequal(t, base.getTarget()))) {
          impls.add(type);
        }
      }

      env.getMessager().printMessage(Kind.NOTE, String.format("types for %s: %s", base, impls));

      // fetch all of the types that implement this base.

      // final VisitorShapeGenerator shape = new VisitorShapeGenerator(env);
      // for (final ReflectedVisitable.Visitor visitor : base.getVisitors()) {
      // // new VisitorGenerator(base, visitor);
      // }

    }

    // calculate

  }

  private static boolean rawequal(TypeName a, TypeName b) {
    return (rawtype(a).reflectionName().equals(rawtype(b).reflectionName()));
  }

  private static ClassName rawtype(TypeName a) {
    if (a instanceof ClassName) {
      return (ClassName) a;
    } else if (a instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) a).rawType;
    }
    throw new AssertionError(String.format("%s (%s) doesn't have a raw type", a.getClass(), a));
  }

  /**
   * calculates if we should emit now.
   *
   * @param processingOver
   * @param lastCount
   * @param emptyQueue
   * @return
   */

  private boolean shouldGenerate(boolean processingOver, int lastCount, boolean emptyQueue) {

    if (emptyQueue) {
      return false;
    }

    if (processingOver) {
      return true;
    }

    if (lastCount == 0) {
      return true;
    }

    return false;

  }

  // old:

  public boolean done(String className) {
    return this.generated.contains(className);
  }

  public boolean add(String k) {
    return this.generated.add(k);
  }

}
