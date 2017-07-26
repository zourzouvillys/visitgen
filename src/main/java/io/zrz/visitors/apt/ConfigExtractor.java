package io.zrz.visitors.apt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import com.google.auto.common.MoreElements;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import io.zrz.visitors.VisitorGenerator;
import io.zrz.visitors.VisitorSpec;
import io.zrz.visitors.VisitorSpec.VisitorSpecBuilder;
import io.zrz.visitors.apt.VisitableConf.VisitableConfBuilder;

public class ConfigExtractor {

  public static VisitableConf specs(TypeElement tbase, ProcessingEnvironment env) {

    final VisitableConfBuilder vb = VisitableConf.builder();

    vb.outputPackage(MirrorStuff.getFullPackageName(MoreElements.asPackage(tbase.getEnclosingElement())));

    for (final AnnotationMirror ant : tbase.getAnnotationMirrors()) {

      if (!ant.getAnnotationType().toString().equals("io.zrz.visitors.annotations.Visitable.Base")) {
        continue;
      }

      for (final AnnotationValue mirror : MirrorStuff.getAnnotationListKeyValue(ant, "visitors")) {

        final VisitorSpec spec = processVisitorAnnotation(env, tbase, (AnnotationMirror) mirror.getValue());

        if (spec != null) {
          vb.visitorSpec(spec);
        }

      }

      for (final AnnotationValue mirror : MirrorStuff.getAnnotationListKeyValue(ant, "value")) {

        final TypeElement type = (TypeElement) env.getTypeUtils().asElement((TypeMirror) mirror.getValue());
        final Optional<ExecutableElement> method = MirrorStuff.getFunctionalInterface(type);

        if (!method.isPresent()) {
          env.getMessager().printMessage(Kind.ERROR, "Visitor model isn't a functional interface", tbase);
          continue;
        }

        final ClassName targetName = ClassName.get("io.zrz.testxxx", "MyNodeVisitors", "MYTESTVisitor");

        final VisitorSpec from = from(env, targetName, type, method.get(), 0, null);

        if (from != null) {
          vb.visitorSpec(from);
        }

      }

    }

    return vb.build();

  }

  private static VisitorSpec processVisitorAnnotation(ProcessingEnvironment env, TypeElement tbase, AnnotationMirror vant) {

    final TypeElement type = (TypeElement) env.getTypeUtils().asElement(MirrorStuff.getAnnotationKeyValue(vant, "value", TypeMirror.class).orElse(null));

    final Optional<ExecutableElement> method = MirrorStuff.getFunctionalInterface(type);

    if (!method.isPresent()) {
      env.getMessager().printMessage(Kind.ERROR, String.format("@Visitable.Visitor '%s' isn't @FunctionalInterface, %s", type, tbase), tbase);
      return null;
    }

    final String className = MirrorStuff.getAnnotationKeyValue(vant, "className", String.class)
        .orElse(type.getSimpleName().toString() + tbase.getSimpleName().toString() + "Visitor");

    final String packageName = MirrorStuff.getAnnotationKeyValue(vant, "packageName", String.class)
        .orElse(MoreElements.getPackage(tbase).getQualifiedName().toString());

    env.getMessager().printMessage(Kind.NOTE, String.format("className=%s, packageName=%s", className, packageName), tbase);

    final ClassName targetName = ClassName.get(packageName, "MyNodeVisitors", className);
    final Optional<Integer> bind = MirrorStuff.getAnnotationKeyValue(vant, "bindParam");
    final Optional<TypeMirror> bindType = MirrorStuff.getAnnotationKeyValue(vant, "bindType");

    env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Skpping " + packageName);

    return from(env, targetName, type, method.get(), bind.orElse(0), bindType.map(mirror -> resolve(env, mirror)).orElse(null));

  }

  /**
   * creates the visitor spec from the annotation.
   *
   *
   *
   * @param targetName
   * @param iface
   * @param method
   * @param bindParam
   * @param bindType
   * @return
   */

  private static VisitorSpec from(ProcessingEnvironment env, ClassName targetName, TypeElement iface, ExecutableElement method, int bindParam,
      TypeElement bindType) {

    final TypeName returnType = TypeName.get(method.getReturnType());

    final VisitorSpecBuilder vb = VisitorSpec.builder()
        .name(targetName)
        .methodNamePrefix(method.getSimpleName().toString())
        .returnType(returnType);

    if (returnType instanceof TypeVariableName) {
      vb.typeParameter((TypeVariableName) returnType);
    }

    final TypeName[] vartypes = new TypeName[method.getParameters().size()];

    for (final TypeParameterElement t : iface.getTypeParameters()) {
      final TypeName tt = TypeName.get(t.asType());
    }

    for (int i = 0; i < method.getParameters().size(); ++i) {

      final VariableElement param = method.getParameters().get(i);

      if (i == bindParam) {

        vb.methodParameter(VisitorGenerator.param(param.getSimpleName().toString()));

        // placeholder for our real type
        vartypes[i] = null;

      } else {

        final TypeName paramtype = TypeName.get(param.asType());

        vartypes[i] = paramtype;

        if (paramtype instanceof TypeVariableName) {
          vartypes[i] = paramtype;
          vb.typeParameter((TypeVariableName) paramtype);
        }

        vb.methodParameter(paramtype, param.getSimpleName().toString());
      }

    }

    if (bindType != null) {

      final TypeName type = TypeName.get(bindType.asType());

      final Optional<ExecutableElement> oexec = functionalMethod(env, bindType);

      if (!oexec.isPresent()) {
        env.getMessager().printMessage(Kind.ERROR, String.format("@Visitable.Visitor with bindType='%s' isn't @FunctionalInterface. %s", bindType, bindType),
            iface);
        return null;
      }

      final ExecutableElement exec = oexec.get();

      // we now need to map the types for the visitor interface to the bound
      // one.

      System.err.println(String.format("SOURCE PARAMS: %s", method.getParameters().stream().map(e -> e.asType()).collect(Collectors.toList())));
      System.err.println(String.format("TARGET PARAMS: %s", exec.getParameters().stream().map(e -> e.asType()).collect(Collectors.toList())));

      final Map<String, TypeName> bindvarmap = new HashMap<>();

      for (int i = 0; i < exec.getParameters().size(); i++) {

        final VariableElement param = exec.getParameters().get(i);

        final TypeName paramtype = TypeName.get(param.asType());

        if (paramtype instanceof TypeVariableName) {
          System.err.println(String.format("P[%d] = %s -> [%s]", i, paramtype, vartypes[i + 1]));
          bindvarmap.put(((TypeVariableName) paramtype).name, vartypes[i + 1]);
        }

      }

      final TypeName retType = TypeName.get(exec.getReturnType());

      if (retType instanceof TypeVariableName) {
        bindvarmap.put(((TypeVariableName) retType).name, TypeName.get(method.getReturnType()));
      }

      if (type instanceof ParameterizedTypeName) {
        final ParameterizedTypeName partype = (ParameterizedTypeName) type;
        final List<TypeName> ptypes = new LinkedList<>();
        for (final TypeName t : partype.typeArguments) {
          if (t instanceof TypeVariableName) {
            final TypeVariableName tvar = (TypeVariableName) t;
            System.err.println(String.format("TVAR %s -> %s", tvar.name, bindvarmap.get(tvar.name)));
            ptypes.add(bindvarmap.get(tvar.name));
          } else {
            ptypes.add(t);
          }
        }
        final ParameterizedTypeName res = ParameterizedTypeName.get(partype.rawType, ptypes.toArray(new TypeName[0]));
        System.err.println("REsult: " + res);
        vb.boundType(res);
      } else {
        vb.boundType(type);
      }

      for (final TypeMirror ptype : exec.getParameters().stream().map(e -> e.asType()).collect(Collectors.toList())) {

        System.err.println(ptype.getKind());

      }

      System.err.println(String.format("SOURCE RET: %s", method.getReturnType()));
      System.err.println(String.format("TARGET RET: %s", exec.getReturnType()));

    }

    return vb.build();

  }

  /**
   * must be a functional interface.
   *
   * may have parameters.
   *
   * @param env
   * @param bindMirror
   * @return
   */

  private static TypeElement resolve(ProcessingEnvironment env, TypeMirror bindMirror) {
    // resolve the type
    return Objects.requireNonNull(env.getElementUtils().getTypeElement(bindMirror.toString()));
  }

  private static Optional<ExecutableElement> functionalMethod(ProcessingEnvironment env, TypeElement type) {

    if (type.getKind() != ElementKind.INTERFACE || type.getAnnotation(FunctionalInterface.class) == null) {
      env.getMessager().printMessage(Kind.ERROR, String.format("@Visitable.Visitor ... %s %s %s",
          type.getKind(),
          type.getAnnotation(FunctionalInterface.class)),
          type);
      return Optional.empty();
    }

    final Optional<ExecutableElement> method = type.getEnclosedElements().stream()
        .filter(e -> e instanceof ExecutableElement)
        .map(e -> ExecutableElement.class.cast(e))
        .filter(e -> e.getModifiers().contains(Modifier.ABSTRACT))
        .filter(e -> !e.getModifiers().contains(Modifier.DEFAULT))
        .filter(e -> !e.getModifiers().contains(Modifier.STATIC))
        .findAny();

    return method;

  }

}
