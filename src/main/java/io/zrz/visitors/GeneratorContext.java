package io.zrz.visitors;

import java.util.Collection;
import java.util.function.Function;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GeneratorContext {

  private Collection<TypeName> types;
  private TypeName interfaceType;
  private Function<String, MethodSpec> extraMethods;

}
