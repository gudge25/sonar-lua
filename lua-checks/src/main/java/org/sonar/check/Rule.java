package org.sonar.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Rule {
  String key();

  String name() default "";

  String description() default "";

  Priority priority() default Priority.MAJOR;

  String[] tags() default {};

  @Deprecated
  double remediationEffort() default -1;
}
