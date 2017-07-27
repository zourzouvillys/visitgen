package io.zrz.visitors.apt;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;

import com.google.common.base.Strings;

import io.zrz.visitors.apt.ReflectedVisitable.Base;
import io.zrz.visitors.apt.ReflectedVisitable.Type;
import lombok.Getter;

@Getter
public class GenerationContext {

  private final ProcessingEnvironment env;

  /**
   * the base interface type we are generating a visitor for.
   */

  private final Base base;

  /**
   * the types that are implemented.
   */

  private final Set<Type> impls;

  public GenerationContext(ProcessingEnvironment env, Base base, Set<Type> impls) {

    this.env = env;

    this.base = base;

    this.impls = impls.stream().map(type -> {

      return type.withName(Strings.emptyToNull(type.getName().trim()))
          .withParamName(Strings.emptyToNull(type.getParamName().trim()));

    }).collect(Collectors.toSet());

  }

}
