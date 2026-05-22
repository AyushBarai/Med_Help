package com.medhelp.medhelp;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface value {

    String val() default "";

}
