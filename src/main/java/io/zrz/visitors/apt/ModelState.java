package io.zrz.visitors.apt;

import javax.annotation.processing.RoundEnvironment;

/**
 * Keeps track of the model so we can hack around incremental compilation in
 * eclipse.
 *
 * @author theo
 *
 */

public class ModelState {

  public void round(RoundEnvironment env) {
  }

}
