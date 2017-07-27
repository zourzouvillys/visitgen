package io.zrz.visitors.apt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import io.zrz.visitors.VisitorGenerator;
import io.zrz.visitors.VisitorSpec;
import io.zrz.visitors.VisitorSpec.VisitorSpecBuilder;

/**
 * given a functional interface, generates the VisitorSpec for it.
 */

public class VisitorShapeGenerator {

  private final ProcessingEnvironment env;

  /**
   *
   */

  VisitorShapeGenerator(ProcessingEnvironment env) {
    this.env = env;
  }

  /**
   *
   */

  /**
   * calculates type stuffs.
   */

  public ReflectedVisitable.VisitorBinder shape(ClassName iface, TypeElement bindType, int bindParam) {

    // given the raw name
    final TypeElement ifaceType = this.env.getElementUtils().getTypeElement(iface.reflectionName());

    // the method
    final ExecutableElement method = MirrorStuff.getFunctionalInterface(ifaceType).orElse(null);

    // the return type.
    final TypeName returnType = TypeName.get(method.getReturnType());

    final VisitorSpecBuilder vb = VisitorSpec.builder()
        .methodNamePrefix(method.getSimpleName().toString())
        .returnType(returnType);

    if (returnType instanceof TypeVariableName) {
      vb.typeParameter((TypeVariableName) returnType);
    }

    final TypeName[] vartypes = new TypeName[method.getParameters().size()];

    for (final TypeParameterElement t : ifaceType.getTypeParameters()) {
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

      final Optional<ExecutableElement> oexec = functionalMethod(this.env, bindType);

      if (!oexec.isPresent()) {
        this.env.getMessager().printMessage(Kind.ERROR,
            String.format("@Visitable.Visitor with bindType='%s' isn't @FunctionalInterface. %s", bindType, bindType),
            ifaceType);
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

    return null;

  }

  /**
   * extract the executable element for the given type.
   */

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
