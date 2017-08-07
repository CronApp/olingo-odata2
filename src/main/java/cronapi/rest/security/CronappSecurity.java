package cronapi.rest.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para definição de segurança em serviços REST.
 *
 * @author arthemus
 * @since 01/08/17
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CronappSecurity {
  
  String get() default "Authenticated";
  
  String post() default "Authenticated";
  
  String put() default "Authenticated";
  
  String delete() default "Authenticated";
  
}