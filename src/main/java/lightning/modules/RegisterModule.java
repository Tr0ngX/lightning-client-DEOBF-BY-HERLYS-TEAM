package lightning.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterModule {
    String name();

    String description();

    Module.Category category();

    boolean persistent();

    boolean toggled();

    boolean drawn();

    int bind();

    boolean proxyEnhanced();

}
