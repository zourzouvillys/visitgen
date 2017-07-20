package io.zrz.visitors;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Generated;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import com.squareup.javapoet.TypeVariableName;

import io.zrz.visitors.generators.DefaultVisitorImplGenerator;
import io.zrz.visitors.generators.SimpleVisitorGenerator;
import lombok.SneakyThrows;

public class VisitorGenerator {

  private static String packageName = "io.joss.graphql.core.schema";

  private static String refPackage = "io.joss.graphql.core.schema.model";

  private static String[] names = {
      "Input", "Object", "Enum", "Interface", "Scalar", "Union"
  };

  private final TypeSpec.Builder container;

  private Collection<TypeName> types;
  private TypeName interfaceType;

  public VisitorGenerator(Builder container) {
    this.container = container;
  }

  public static void main(String[] args) {

    final VisitorImplementationGenerator[] impls = {
        DefaultVisitorImplGenerator.builder()
            .name(spec -> GeneratorUtils.prefixName("DefaultAbstract", spec.getName()))
            .build(),
        DefaultVisitorImplGenerator.builder()
            .name(spec -> GeneratorUtils.prefixName("Default", spec.getName()))
            .extendsType(spec -> GeneratorUtils.prefixName("DefaultAbstract", spec.getName(), spec.typeVariables()))
            .defaultBody(spec -> GeneratorUtils.defaultReturn(spec))
            .filterTypes(type -> false)
            .build(),
    };

    VisitorGenerator

        .withContainer(TypeSpec.classBuilder("TypeVisitors").addModifiers(Modifier.PUBLIC, Modifier.FINAL))

        .forTypes(
            ClassName.get(refPackage, "Type"),
            Arrays.stream(names).map(name -> ClassName.get(refPackage, name + "Type")).collect(Collectors.toList()))

        .withGenerator(VisitorSpec.builder()
            .name(packageName, "TypeVisitors", "GenericReturnVisitor")
            .methodNamePrefix("visit")
            .typeParameter("R")
            .genericReturnType("R")
            .methodParameter(param("value"))
            .implementations(Arrays.asList(impls))
            .build())

        .withGenerator(VisitorSpec.builder()
            .name(packageName, "TypeVisitors", "NoReturnVisitor")
            .methodNamePrefix("visit")
            .methodParameter(param("value"))
            .implementations(Arrays.asList(impls))
            .build())

        .withGenerator(VisitorSpec.builder()
            .name(packageName, "TypeVisitors", "NoReturnGenericArgVisitor")
            .methodNamePrefix("visit")
            .methodParameter(param("value"))
            .genericReturnType("R")
            .typeParameter("R")
            .typeParameter(TypeVariableName.get("V"))
            .methodParameter(typeVarParam("V", "param"))
            .implementations(Arrays.asList(impls))
            .build())

        .writeTo(packageName, Paths.get("/Users/theo/git/graphql/graphql-core/src/main/java/"));

    // ----

    VisitorGenerator

        .withContainer(TypeSpec.classBuilder("TypeRefVisitors").addModifiers(Modifier.PUBLIC, Modifier.FINAL))

        .forTypes(
            ParameterizedTypeName.get(ClassName.get(refPackage, "TypeRef"), TypeVariableName.get("T")),
            Arrays.asList(
                ParameterizedTypeName.get(ClassName.get(refPackage, "SimpleTypeRef"), TypeVariableName.get("T")),
                ParameterizedTypeName.get(ClassName.get(refPackage, "GenericTypeRef"), TypeVariableName.get("T"))))

        .withGenerator(VisitorSpec.builder()
            .name(packageName, "TypeRefVisitors", "GenericTypRefReturnVisitor")
            .methodNamePrefix("visit")
            .typeParameter(TypeVariableName.get("T", ClassName.get(refPackage, "Type")))
            .typeParameter("R")
            .genericReturnType("R")
            .methodParameter(param("value"))
            .implementations(Arrays.asList(impls))
            .build())

        .withGenerator(VisitorSpec.builder()
            .name(packageName, "TypeRefVisitors", "NoReturnVisitor")
            .methodNamePrefix("visit")
            .methodParameter(param("value"))
            .typeParameter(TypeVariableName.get("T", ClassName.get(refPackage, "Type")))
            .implementations(Arrays.asList(impls))
            .build())

        .withGenerator(VisitorSpec.builder()
            .name(packageName, "TypeRefVisitors", "NoReturnGenericArgVisitor")
            .methodNamePrefix("visit")
            .methodParameter(param("value"))
            .genericReturnType("R")
            .typeParameter(TypeVariableName.get("T", ClassName.get(refPackage, "Type")))
            .typeParameter("R")
            .typeParameter(TypeVariableName.get("V"))
            .methodParameter(typeVarParam("V", "param"))
            .implementations(Arrays.asList(impls))
            .build())

        .writeTo(packageName, Paths.get("/Users/theo/git/graphql/graphql-core/src/main/java/"));

  }

  public static Function<TypeName, ParameterSpec> param(String paramName) {
    return type -> ParameterSpec.builder(type, paramName).build();
  }

  public static Function<TypeName, ParameterSpec> typeVarParam(String typeVariable, String paramName) {
    return type -> ParameterSpec.builder(TypeVariableName.get(typeVariable), paramName).build();
  }

  /**
   *
   * @param packageName
   * @param path
   */

  @SneakyThrows
  public void writeTo(String packageName, Path path) {
    JavaFile.builder(packageName, this.container.build()).build().writeTo(path);
  }

  @SneakyThrows
  public void writeTo(String packageName, Filer filer) {
    JavaFile.builder(packageName, this.container.build()).build().writeTo(filer);
  }

  public VisitorGenerator withGenerator(VisitorSpec spec) {

    final TypeSpec out = new SimpleVisitorGenerator(spec).generate(this.types).build();

    this.container.addType(out);

    spec.getImplementations().forEach(implgen -> {

      this.container.addType(implgen.generate(spec, new GeneratorContext(this.types, this.interfaceType, null)));

    });

    return this;

  }

  public static VisitorGenerator withContainer(Builder container) {
    container.addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "$S", "io.zrz.visitors").build());
    return new VisitorGenerator(container);
  }

  public VisitorGenerator forTypes(TypeName baseType, Collection<TypeName> types) {
    this.interfaceType = baseType;
    this.types = types;
    return this;
  }

}
