package io.zrz.visitors.apt;

import java.util.List;

import io.zrz.visitors.VisitorSpec;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class VisitableConf {
  private String outputPackage;
  @Singular
  final List<VisitorSpec> visitorSpecs;
}
