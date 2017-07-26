package io.zrz.visitors.apt;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;

/**
 * Keeps track of the model so we can hack around incremental compilation in
 * eclipse.
 *
 * @author theo
 *
 */

public class ModelState {

  private final Set<String> generated = new HashSet<>();

  public void round(RoundEnvironment env) {
  }

  public boolean done(String className) {
    return this.generated.contains(className);
  }

  public boolean add(String k) {
    return this.generated.add(k);
  }

}
