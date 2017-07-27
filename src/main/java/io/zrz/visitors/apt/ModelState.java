package io.zrz.visitors.apt;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import lombok.SneakyThrows;

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

  private final Map<String, ReflectedVisitable.Base> bases = new HashMap<>();
  private final Map<String, ReflectedVisitable.Type> types = new HashMap<>();

  public void round(ProcessingEnvironment env, RoundEnvironment round, AnnotationScanner scanner) {

    if (this.done) {
      // it's a new entry into the processor.
      this.done = false;
    }

    for (final Map.Entry<String, ReflectedVisitable.Base> e : scanner.bases.entrySet()) {

      if (this.bases.containsKey(e.getKey())) {
        env.getMessager().printMessage(Kind.ERROR, String.format("@Visitable.Base %s has already been generated",
            e.getKey()), e.getValue().getElement());
        return;
      }

    }

    for (final Map.Entry<String, ReflectedVisitable.Type> e : scanner.types.entrySet()) {

      if (this.types.containsKey(e.getKey())) {
        env.getMessager().printMessage(Kind.ERROR, String.format("@Visitable.Type %s has already been generated",
            e.getKey()), e.getValue().getElement());
        return;
      }

    }

    this.bases.putAll(scanner.bases);
    this.types.putAll(scanner.types);

    if (this.shouldGenerate(round.processingOver(), round.getRootElements().size(), this.bases.isEmpty() && this.types.isEmpty())) {
      this.generate(env);
    }

    if (round.processingOver()) {
      // we're done.
      this.done = true;
    }

  }

  /**
   * calculates the models, and generates the code.
   */

  @SneakyThrows
  private void generate(ProcessingEnvironment env) {

    // normalize the config.

    for (final ReflectedVisitable.Base base : this.bases.values()) {

      final Set<ReflectedVisitable.Type> impls = new HashSet<>();

      // find the implementing types.
      for (final ReflectedVisitable.Type type : this.types.values()) {
        if (type.getBases().stream().anyMatch(t -> ModelState.rawequal(t, base.getTarget()))) {
          impls.add(type);
        }
      }

      if (impls.isEmpty()) {
        // a warning
        env.getMessager().printMessage(Kind.WARNING, String.format("Base %s has no implemented types",
            base.getClassName()),
            base.getElement(),
            base.getAnnotation());
        continue;
      }

      final GenerationContext ctx = new GenerationContext(env, base, impls);

      final BaseGenerator bgen = new BaseGenerator(ctx);

      final TypeSpec typesClass = TypeSpec.classBuilder(base.getClassName())
          .addType(bgen.generateInvoker(ctx))
          .addType(bgen.generateTypes(ctx))
          .build();

      JavaFile.builder(base.getClassName().packageName(), typesClass).build().writeTo(env.getFiler());

      // final VisitorShapeGenerator shape = new VisitorShapeGenerator(env);

      final VisitorWriter writer = new VisitorWriter(ctx);

      // ---

      for (final ReflectedVisitable.Visitor visitor : base.getVisitors()) {

        final Collection<TypeSpec> typespecs = writer.generate(ctx, visitor);

        for (final TypeSpec spec : typespecs) {

          env.getMessager().printMessage(Kind.WARNING, String.format("Generating %s",
              visitor.getClassName()),
              base.getElement(),
              base.getAnnotation());

          JavaFile.builder(visitor.getClassName().packageName(), spec).build().writeTo(env.getFiler());

        }

      }

    }

    this.bases.clear();
    this.types.clear();

    // calculate

  }

  private static boolean rawequal(TypeName a, TypeName b) {
    return (rawtype(a).reflectionName().equals(rawtype(b).reflectionName()));
  }

  public static ClassName rawtype(TypeName a) {
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
