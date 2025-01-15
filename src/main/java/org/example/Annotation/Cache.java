package main.java.org.example.Annotation;



import main.java.org.example.Proxy.Utils.CacheType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
    CacheType cacheType() default CacheType.IN_MEMORY; // CacheType.FILE для работы в файлах
    String fileNamePrefix() default ""; // Задает начало файлу или архиву
    boolean zip() default false; // Данные будут заархивированы
    Class<?>[] identityBy() default {}; // Какой класс будет важным для уникальности
    int listList() default Integer.MAX_VALUE; // Ограничивает капасити списка
}
