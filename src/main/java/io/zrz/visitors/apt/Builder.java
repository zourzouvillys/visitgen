package io.zrz.visitors.apt;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import io.zrz.visitors.GeneratorContext;
import io.zrz.visitors.GeneratorUtils;
import io.zrz.visitors.VisitorGenerator;
import io.zrz.visitors.VisitorSpec;
import io.zrz.visitors.VisitorSpec.VisitorSpecBuilder;
import io.zrz.visitors.generators.SimpleVisitorGenerator;
import lombok.SneakyThrows;

public class Builder {

  private final String packageName = "io.zrz.test";

  private final ProcessingEnvironment env;

  public Builder(ProcessingEnvironment processingEnv) {
    this.env = processingEnv;
  }

  /**
   * Pass in a functional interface, get out a VisitorSpec.
   *
   * @param funcintf
   * @return
   */

  public VisitorSpec fromFunctionalInterface(Class<?> funcintf) {
    return this.fromFunctionalInterface(funcintf, -1, null);
    // return VisitorSpec.builder()
    // .name(ClassName.get(this.packageName, "MyNodeVisitors",
    // funcintf.getSimpleName()))
    // .methodNamePrefix("test")
    // .typeParameter("T")
    // .methodParameter(typeVarParam("T", "t"))
    // .returnType(TypeName.BOOLEAN)
    // .build();
  }

  /**
   * Generates a visitor from a functional interface, using the given parameter
   * number as the input, and using the specified class as the bound type.
   *
   * @param funcintf
   * @param bindParamNumber
   * @return
   */

  public VisitorSpec fromFunctionalInterface(Class<?> funcintf, int bindParamNumber, Class<?> boundType) {

    final Method method = GeneratorUtils.lookup(funcintf);

    final TypeName returnType = TypeName.get(method.getGenericReturnType());

    final VisitorSpecBuilder vb = VisitorSpec.builder()
        .name(this.makeName(funcintf.getSimpleName()))
        .methodNamePrefix(method.getName())
        .returnType(returnType);

    if (boundType != null) {
      vb.boundType(ClassName.get(boundType));
    }

    for (final Type t : method.getGenericParameterTypes()) {

      final TypeName tt = TypeName.get(t);

      if (tt instanceof TypeVariableName) {

        vb.typeParameter((TypeVariableName) tt);

      }

    }

    for (int i = 0; i < method.getParameterCount(); ++i) {

      if (i == bindParamNumber) {
        vb.methodParameter(VisitorGenerator.param("value"));
      } else {
        vb.methodParameter(TypeName.get(method.getGenericParameterTypes()[i]), method.getParameters()[i].getName());
      }

    }

    if (returnType instanceof TypeVariableName) {
      vb.typeParameter((TypeVariableName) returnType);
    }

    return vb.build();
  }

  public VisitorSpec fromFunctionalInterface(Class<?> funcintf, Class<?> boundType) {
    return this.fromFunctionalInterface(funcintf, 0, boundType);
  }

  private ClassName makeName(String string) {
    return ClassName.get(this.packageName, "MyNodeVisitors", string + "Visitor");
  }

  @SneakyThrows
  public void add(String base, Set<String> types) {

    final ClassName klass = ClassName.bestGuess(base);
    final List<TypeName> children = types.stream().map(name -> ClassName.bestGuess(name)).collect(Collectors.toList());

    final TypeElement tbase = this.env.getElementUtils().getTypeElement(base);

    final TypeSpec.Builder container = TypeSpec.classBuilder("MyNodeVisitors")
        .addModifiers(Modifier.PUBLIC);

    // env.getElementUtils().getBinaryName(elt)

    final Types tt = this.env.getTypeUtils();

    final List<VisitorSpec> lspecs = Lists.newArrayList();

    lspecs.addAll(ConfigExtractor.specs(tbase, this.env));

    // for (final Class<?> v : ant.value()) {
    // lspecs.add(this.fromFunctionalInterface(v));
    // }

    final VisitorSpec[] specs = lspecs.toArray(new VisitorSpec[0]);

    final VisitorSpec[] aspecs = {

        // this.fromFunctionalInterface(Predicate.class),
        this.fromFunctionalInterface(Predicate.class, 0, BooleanSupplier.class),
        // this.fromFunctionalInterface(BiPredicate.class),
        this.fromFunctionalInterface(BiPredicate.class, 0, Predicate.class),

        // this.fromFunctionalInterface(Consumer.class),
        this.fromFunctionalInterface(Consumer.class, Runnable.class),

        this.fromFunctionalInterface(BiConsumer.class, Consumer.class),
        this.fromFunctionalInterface(Supplier.class),
        this.fromFunctionalInterface(IntSupplier.class),
        this.fromFunctionalInterface(BooleanSupplier.class),
        this.fromFunctionalInterface(IntUnaryOperator.class),
        this.fromFunctionalInterface(ObjIntConsumer.class, IntConsumer.class),
        this.fromFunctionalInterface(ToIntFunction.class, IntSupplier.class),
        this.fromFunctionalInterface(IntToLongFunction.class, Runnable.class),
        this.fromFunctionalInterface(Function.class, Supplier.class),
        this.fromFunctionalInterface(BiFunction.class, Function.class),

    };

    container.addType(this.generateBinder(specs, klass, ClassName.get(this.packageName, "MyNodeVisitors", "Invoker")));

    final TypeSpec.Builder enumtype = TypeSpec.enumBuilder("Type")
        .addModifiers(Modifier.PUBLIC);

    enumtype.addSuperinterface(ClassName.get(this.packageName, "MyNodeVisitors", "Invoker"));

    children.forEach(child -> {
      enumtype.addEnumConstant(((ClassName) child).simpleName(), this.buildMethods(enumtype, specs, klass, child));
    });

    enumtype.addMethods(this.buildEnumMethods(klass, children, specs));

    Arrays.stream(specs).forEach(spec -> {

      final TypeSpec.Builder out = new SimpleVisitorGenerator(spec).generate(children);

      spec.getImplementations().forEach(implgen -> {

        final GeneratorContext ctx = GeneratorContext.builder()
            .types(children)
            .interfaceType(klass)
            .build();

        try {
          JavaFile.builder(this.packageName, implgen.generate(spec, ctx)).build().writeTo(this.env.getFiler());
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }

      });

      out.addMethod(this.generateMainClassInvoker(klass, spec));

      if (spec.getBoundType() != null) {
        out.addMethod(this.generateMainClassBinders(klass, spec));
      }

      container.addType(out.build());

    });

    Arrays.stream(specs).forEach(spec -> {
      // container.addMethod(this.generateMainClassInvoker(klass, spec));
      if (spec.getBoundType() != null) {
        // container.addMethod(this.generateMainClassBinders(klass, spec));
      }
    });

    Arrays.stream(specs).forEach(spec -> {
      if (spec.getBoundType() != null) {
        if (!spec.getTypeParameters().isEmpty() && spec.getTypeParameters().get(0).name.equals("T") && !spec.getMethodParameters().isEmpty()) {
          // container.addMethod(this.generateMainClassBoundInvokers(klass,
          // spec));
          // container.addMethod(this.generateMainClassSelfBinders(klass,
          // spec));
        }
      }
    });

    container.addType(enumtype.build());

    JavaFile.builder(this.packageName, container.build()).build().writeTo(this.env.getFiler());

  }

  private MethodSpec generateMainClassInvoker(ClassName klass, VisitorSpec spec) {

    final MethodSpec.Builder method = MethodSpec.methodBuilder(spec.applyName());
    method.addModifiers(Modifier.PUBLIC);
    method.addModifiers(Modifier.STATIC);

    method.addJavadoc(String.format("generateMainClassInvoker(%s, %s)", klass, spec.getName()));

    method.returns(spec.returnType());
    method.addTypeVariables(spec.typeVariables());

    method.addParameter(ParameterSpec.builder(spec.toType(a -> a), "visitor").build());
    method.addParameter(ParameterSpec.builder(klass, "instance").build());

    final ClassName child = klass;

    method.addParameters(spec.getMethodParameters().stream()
        .map(a -> a.apply(child))
        .filter(a -> !a.type.equals(klass))
        .collect(Collectors.toList()));

    final String out = spec.getMethodParameters().stream()
        .map(a -> a.apply(child))
        .map(p -> p.type.equals(klass) ? "instance" : p.name)
        .collect(Collectors.joining(", "));

    method.addStatement(
        ((spec.returnType() == TypeName.VOID) ? "" : "return ")
            + "Type.fromInstance(instance).$L(visitor$L)",
        spec.applyName(),
        out.equals("") ? "" : (", " + out));

    return method.build();
  }

  /**
   * the bind() instances that go into the main class. These take an instance
   * and a visitor, and return a lambda which when invoked calls the visitor
   * with the instance provided to this method.
   *
   * If there are any other arguments on the functional interface, then they are
   * passed through too.
   *
   * @param klass
   * @param spec
   * @return
   */

  private MethodSpec generateMainClassBinders(ClassName klass, VisitorSpec spec) {

    final MethodSpec.Builder method = MethodSpec.methodBuilder("bind" + (spec.getMethodNameSuffix() != null ? spec.getMethodNameSuffix() : ""));

    method.addJavadoc(String.format("generateMainClassBinders(%s, %s)", klass, spec.getName()));

    method.addModifiers(Modifier.PUBLIC);
    method.addModifiers(Modifier.STATIC);

    final TypeName returnType = spec.calculateBoundReturnType(klass);

    method.returns(returnType);
    method.addTypeVariables(spec.calculateBoundTypeVariables(klass));

    method.addParameter(ParameterSpec.builder(spec.toType(a -> a), "visitor").build());

    method.addParameter(ParameterSpec.builder(klass, "instance").build());

    final ClassName child = klass;

    method.addStatement("final Type type = Type.fromInstance(instance)");

    final String in = spec.getMethodParameters().stream()
        .map(a -> a.apply(child))
        .filter(a -> !a.type.equals(klass))
        .map(p -> p.name)
        .collect(Collectors.joining(", "));

    final String out = spec.getMethodParameters().stream()
        .map(a -> a.apply(child))
        .map(p -> p.type.equals(klass) ? "instance" : p.name)
        .collect(Collectors.joining(", ", ", ", ""));

    method.addStatement(
        "return ($L) -> type.$L(visitor$L)",
        in,
        spec.applyName(),
        out);

    return method.build();

  }

  // generate the Invoker interface

  private TypeSpec generateBinder(VisitorSpec[] specs, ClassName className, ClassName invokerClass) {

    final TypeSpec.Builder iface = TypeSpec.interfaceBuilder(invokerClass);

    iface.addModifiers(Modifier.STATIC);
    iface.addModifiers(Modifier.PUBLIC);

    for (final VisitorSpec spec : specs) {

      final MethodSpec.Builder applyMethod = MethodSpec.methodBuilder(spec.applyName());

      applyMethod.addJavadoc(String.format("generateBinder(%s, %s)", className, spec.getName()));

      applyMethod.addModifiers(Modifier.PUBLIC);
      applyMethod.addModifiers(Modifier.ABSTRACT);
      applyMethod.returns(spec.returnType());
      applyMethod.addTypeVariables(spec.typeVariables());

      applyMethod.addParameter(ParameterSpec.builder(spec.toType(a -> a), "visitor").build());

      spec.getMethodParameters().forEach(consumer -> applyMethod.addParameter(consumer.apply(className)));

      iface.addMethod(applyMethod.build());

      if (spec.getBoundType() != null) {
        // iface.addMethod(this.generateInvokerDefaultBind(className, spec));
      }

    }

    return iface.build();

  }

  // called for each functional type. returned methods are added to the main
  // class

  private Iterable<MethodSpec> buildEnumMethods(ClassName klass, List<TypeName> children, VisitorSpec[] specs) {
    final List<MethodSpec> methods = Lists.newArrayList();

    //
    {
      final MethodSpec.Builder method = MethodSpec.methodBuilder("fromInstance");
      method.addModifiers(Modifier.STATIC);
      method.addModifiers(Modifier.PUBLIC);
      method.returns(ClassName.bestGuess("Type"));
      method.addParameter(ParameterSpec.builder(klass, "instance").build());

      method.addStatement("java.util.Objects.requireNonNull(instance)");

      final CodeBlock.Builder cb = CodeBlock.builder();

      int count = 0;

      for (final TypeName type : children) {
        if (count++ == 0) {
          cb.beginControlFlow("if (instance instanceof $L)", type);
        } else {
          cb.nextControlFlow("else if (instance instanceof $L)", type);
        }
        cb.addStatement("return $L", ((ClassName) type).simpleName());
      }

      cb.endControlFlow();
      cb.addStatement("throw new RuntimeException(String.format(\"Can't map instance of '%s'\", instance.getClass().getCanonicalName()))");

      method.addCode(cb.build());

      methods.add(method.build());
    }
    {
      final MethodSpec.Builder method = MethodSpec.methodBuilder("fromClass");
      method.addModifiers(Modifier.STATIC);
      method.addModifiers(Modifier.PUBLIC);
      method.addTypeVariable(TypeVariableName.get("T", klass));
      method.returns(ClassName.bestGuess("Type"));
      method.addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("T")), "klass").build());

      method.addStatement("java.util.Objects.requireNonNull(klass)");

      final CodeBlock.Builder cb = CodeBlock.builder();

      int count = 0;

      for (final TypeName type : children) {
        if (count++ == 0) {
          cb.beginControlFlow("if ($L.class.isAssignableFrom(klass))", type);
        } else {
          cb.nextControlFlow("else if ($L.class.isAssignableFrom(klass))", type);
        }
        cb.addStatement("return $L", ((ClassName) type).simpleName());
      }

      cb.endControlFlow();
      cb.addStatement("throw new RuntimeException(String.format(\"Can't map type '%s'\", klass.getCanonicalName()))");

      method.addCode(cb.build());

      methods.add(method.build());
    }
    //

    return methods;
  }

  // builds the body of the enum constant.

  private TypeSpec buildMethods(TypeSpec.Builder container, VisitorSpec[] specs, TypeName base, TypeName child) {
    final TypeSpec.Builder enumtype = TypeSpec.anonymousClassBuilder("");
    for (final VisitorSpec spec : specs) {
      enumtype.addMethod(this.buildEnumConstantApply(spec, base, child));
    }
    return enumtype.build();
  }

  // builds the "apply" method in the enum constant

  private MethodSpec buildEnumConstantApply(VisitorSpec spec, TypeName base, TypeName child) {

    final MethodSpec.Builder method = MethodSpec.methodBuilder(spec.applyName());
    method.addJavadoc(String.format("buildEnumConstantApply(%s, %s)", spec.getName(), child));
    method.addModifiers(Modifier.PUBLIC);
    method.returns(spec.returnType());
    method.addTypeVariables(spec.typeVariables());
    method.addParameter(ParameterSpec.builder(spec.toType(a -> a), "visitor").build());
    final String methodName = GeneratorUtils.methodName(spec, child);

    method.addParameters(spec.getMethodParameters().stream().map(a -> a.apply(base)).collect(Collectors.toList()));

    final String args = spec.getMethodParameters().stream().map(a -> a.apply(child)).map(p -> {
      if (p.type.equals(child)) {
        return String.format("(%s)%s", child, p.name);
      }
      return p.name;
    }).collect(Collectors.joining(", "));

    if (spec.returnType() == TypeName.VOID) {
      method.addStatement("visitor.$L($L)", methodName, args);
    } else {
      method.addStatement("return visitor.$L($L)", methodName, args);
    }
    return method.build();
  }

}
