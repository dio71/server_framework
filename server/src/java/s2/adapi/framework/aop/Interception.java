package s2.adapi.framework.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AOP 대상 메소드를 지정하기 위하여 사용되는 Annotation 이다.
 * tag 값을 사용하여 AOP 지정시 세부적으로 적용여부를 판단할 수 있다.
 * @author kimhd
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Interception {
	String tag() default "";
}
