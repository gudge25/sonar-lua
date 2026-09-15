package org.sonar.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RuleProperty {
  String key() default "";

  String description() default "";

  String defaultValue() default "";

  String type() default "STRING";
}
