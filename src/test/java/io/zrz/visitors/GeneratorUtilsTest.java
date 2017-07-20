package io.zrz.visitors;

import org.junit.Test;

public class GeneratorUtilsTest {

  @FunctionalInterface
  public interface MyClass {

    void moo();

  }

  @Test
  public void test() {

    GeneratorUtils.lookup(MyClass.class);

  }

}
