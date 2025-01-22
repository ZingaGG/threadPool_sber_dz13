package org.example;

import org.example.Impl.ServiceImpl;
import org.example.Impl.ServiceImpl2;
import org.example.Proxy.Utils.CacheProxy;
import org.example.Service.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Main {
     public static final String ROOT_PATH = "CachingProxyAnnotationTask/Cache";
    public static void main(String[] args) throws IOException {
        CacheProxy cacheProxy = new CacheProxy(ROOT_PATH);
        Service service = cacheProxy.cache(new ServiceImpl());

        Service service2 = cacheProxy.cache(new ServiceImpl2());

        System.out.println("Вывод первого метода + разница если другой имплементатор");
        System.out.println(service.run("test", 2));
        System.out.println(service.run("test", 2));

        System.out.println(service.run("test", 3));
        System.out.println(service.run("test", 3));

        System.out.println(service2.run("test", 2));
        System.out.println(service2.run("test", 2));

        System.out.println(service2.run("test", 3));
        System.out.println(service2.run("test", 3));

        System.out.println("\nВывод второго метода на капасити списка");
        System.out.println(service.work("item"));
        System.out.println(service.work("item"));

        System.out.println("\nВывод третьего метода на отсутствие названия файла");
        System.out.println(service.workIntoFile(5));
        System.out.println(service.workIntoFile(5));


        boolean flagForPathClear = true;
        if(flagForPathClear){
            // Очищает директорию от файлов после отработки (для удобства)
            try (Stream<Path> paths = Files.walk(Path.of("CachingProxyAnnotationTask/Cache"))) {
                paths.filter(Files::isRegularFile)
                        .forEach(p -> {try {Files.delete(p);} catch (IOException ignored) {}});
            } catch (IOException ignored) {}
        }
    }
}