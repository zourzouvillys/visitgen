package io.zrz.visitors;

import java.util.Collection;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public interface Generator {

  TypeSpec.Builder generate(Collection<TypeName> types);

}
