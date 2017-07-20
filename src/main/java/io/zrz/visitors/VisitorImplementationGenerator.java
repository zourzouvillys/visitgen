package io.zrz.visitors;

import com.squareup.javapoet.TypeSpec;

public interface VisitorImplementationGenerator {

  TypeSpec generate(VisitorSpec spec, GeneratorContext ctx);

}
