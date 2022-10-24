package github.javaguide.extension;

import java.lang.annotation.*;

/**
 * @author zhp
 * @date 2022-10-24 15:51
 * spi
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
