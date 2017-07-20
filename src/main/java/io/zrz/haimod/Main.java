package io.zrz.haimod;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.lang.model.element.Modifier;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.squareup.javapoet.TypeSpec;

import io.joss.graphql.core.schema.SchemaCompiler;
import io.joss.graphql.core.schema.model.InputType;
import io.joss.graphql.core.schema.model.Model;
import io.joss.graphql.core.schema.model.ObjectType;
import io.joss.graphql.core.schema.model.Type;
import io.saasy.genmod.ActionCreatorGenerator;
import io.saasy.genmod.DispatcherGenerator;
import io.saasy.genmod.EventGenerator;
import io.saasy.genmod.MiddlewareGenerator;
import io.saasy.genmod.ObjectGenerator;
import io.saasy.genmod.ReducerGenerator;
import io.saasy.genmod.TypeGenerator;

public class Main {

  private static final Predicate<Type> isObjectType = (type) -> type.getClass().isAssignableFrom(ObjectType.class);
  private static final Predicate<Type> isInputType = (type) -> type.getClass().isAssignableFrom(InputType.class);

  private static final Function<Type, ObjectType> convertToObjectType = in -> ObjectType.class.cast(in);
  private static final Function<Type, InputType> convertToInputType = in -> InputType.class.cast(in);

  public static void main(String[] args) throws IOException {

    final String packageName = "com.example.helloworld.mutation";

    final Model model = SchemaCompiler.compile(Paths.get("/Users/theo/workspaces/saasy/saasy/docs/schemas/"));
    final Path base = Paths.get("/Users/theo/workspaces/saasy/saasy/src/main/java");
    final GenContext ctx = new GenContext(model, packageName);

    //

    final SetView<Type> diff = Sets.difference(Sets.newHashSet(ctx.getModel().getTypes().values()), ctx.getReferencedTypes());
    diff.forEach(unused -> System.err.println(String.format("Unused: %s", unused.getName())));

    // TODO: see if any types could be better off as inputs

    ////// generate

    final TypeSpec.Builder types = TypeSpec.classBuilder("Types").addModifiers(Modifier.PUBLIC);

    // generall of the input types.
    final TypeGenerator typegen = new TypeGenerator(types);
    model.getInputTypes().forEach(type -> typegen.generate(ctx, type));
    ctx.add(packageName, types.build());

    // only generate object types that are referenced from the registered query
    // mutation, and subscription roots

    //
    final TypeSpec.Builder objects = TypeSpec.classBuilder("Objects").addModifiers(Modifier.PUBLIC);
    final ObjectGenerator objectgen = new ObjectGenerator(objects);
    ctx.getReferencedTypes().stream().filter(isObjectType).map(convertToObjectType).forEach(type -> objectgen.generate(ctx, type));
    ctx.add(packageName, objects.build());

    //

    ctx.getSchemas().stream()
        .forEach(schema -> {
          final MutationRoot mutationRoot = schema.getMutationRoot();
          if (mutationRoot != null) {
            final TypeSpec.Builder container = TypeSpec.classBuilder(mutationRoot.getName()).addModifiers(Modifier.PUBLIC);
            new ActionCreatorGenerator(ctx, mutationRoot).generate(container);
            new EventGenerator(ctx, mutationRoot).generate(container);
            new DispatcherGenerator(ctx, mutationRoot).generate(container);
            new MiddlewareGenerator(ctx, mutationRoot).generate(container);
            new ReducerGenerator(ctx, mutationRoot).generate(container);
            ctx.add(schema.getPackage().orElse(packageName), container.build());
          }
        });

    // write out
    ctx.writeTo(base);

  }

}
