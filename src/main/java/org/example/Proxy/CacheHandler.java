package org.example.Proxy;

import org.example.Annotation.Cache;
import org.example.Proxy.Utils.CacheType;


import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CacheHandler implements InvocationHandler {
    private final Object target;
    private final String rootPath;
    private final Map<String, Object> cache = new HashMap<>();

    public CacheHandler(String rootPath, Object target){
        this.target = target;
        this.rootPath = rootPath;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Cache annotation = method.getAnnotation(Cache.class);

        if(annotation == null){
            return method.invoke(target, args);
        }

        String cacheKey = createKey(method, args, annotation);
        if (annotation.cacheType() == CacheType.IN_MEMORY) {
            return memoryCache(method, args, cacheKey, annotation);
        } else {
            return cacheIntoFile(method, args, cacheKey, annotation);
        }
    }

    private String createKey(Method method, Object[] args, Cache cacheAnnotation) {
        StringBuilder stringBuilder = new StringBuilder(target.getClass().getSimpleName() + "_" + method.getName());
        Set<Class<?>> setOfClasses = Set.of(cacheAnnotation.identityBy());

        if(args == null){
            System.out.println("Key created: " + stringBuilder);
            return stringBuilder.toString();
        }

        for (Object arg : args) {
            if (setOfClasses.isEmpty() || setOfClasses.contains(arg.getClass())) {
                stringBuilder.append("_").append(arg);
            }
        }

        return stringBuilder.toString();
    }
    private Object memoryCache(Method method, Object[] args, String cacheKey, Cache cacheAnnotation) throws InvocationTargetException, IllegalAccessException {
       if(cache.containsKey(cacheKey)){
           System.out.print("Взято из кэша - "); // Для видимости, если взято из кэша
           return cache.get(cacheKey);
       }

       Object methodResult = method.invoke(target, args);
       if(methodResult instanceof List<?> resultList && resultList.size() > cacheAnnotation.listList()){
           methodResult = resultList.subList(0, cacheAnnotation.listList());
       }

       cache.put(cacheKey, methodResult);
       System.out.print("Выполнено впервые - ");
       return methodResult;
    }

    private Object cacheIntoFile(Method method, Object[] args, String cacheKey, Cache cacheAnnotation) throws InvocationTargetException, IllegalAccessException, IOException {
        Path cacheFile;

        if(cacheAnnotation.fileNamePrefix().isEmpty()){
            cacheFile = Paths.get(rootPath, cacheKey);
        } else {
            cacheFile = Paths.get(rootPath, cacheAnnotation.fileNamePrefix() + "_" + cacheKey);
        }


        if (Files.exists(Paths.get(cacheFile + ".zip"))) {
            return readZIPFile(cacheFile);
        }

        if (Files.exists(Paths.get(cacheFile + ".cache"))) {
            return readRegularFile(cacheFile);
        }

        Object result = method.invoke(target, args);
        if (result instanceof List<?> listResult && listResult.size() > cacheAnnotation.listList()) {
            result = listResult.subList(0, cacheAnnotation.listList());
        }

        Files.createDirectories(cacheFile.getParent());

        if(cacheAnnotation.zip()){
            writeRegularFileWithZIP(result, cacheFile);
        } else {
            writeRegularFile(result, cacheFile);
        }

        System.out.print("Выполнено впервые - ");
        return result;
    }

    private void writeRegularFile(Object result, Path cacheFile) throws IOException {
        Path cacheFileCreator = Paths.get(cacheFile + ".cache");
        try (OutputStream outputStream = Files.newOutputStream(cacheFileCreator);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(result);
        } catch (NotSerializableException e) {
            throw new RuntimeException("Несереализуемо, объект должен быть Serializable", e);
        }
    }

    private void writeRegularFileWithZIP(Object result, Path cacheFile) throws IOException {
        Path ZIPCacheName = Paths.get(cacheFile + ".zip");
        Path tempFile = Files.createTempFile("cache", ".tmp");

        try (OutputStream outputStream = Files.newOutputStream(tempFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(result);
        }

        try (OutputStream fileOutputStream = Files.newOutputStream(ZIPCacheName);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
             InputStream inputStream = Files.newInputStream(tempFile)) {
            ZipEntry zipEntry = new ZipEntry(tempFile.getFileName().toString());
            zipOutputStream.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, bytesRead);
            }
            zipOutputStream.closeEntry();
        }

        Files.delete(tempFile);
    }

    private Object readRegularFile(Path cacheFile){
        Path cacheFileName = Paths.get(cacheFile + ".cache");

        try (InputStream inputStream = Files.newInputStream(cacheFileName);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            System.out.print("Взято из кэша - "); // Для видимости, если взято из кэша
            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка в чтение кэша с файла: " + cacheFile, e);
        }
    }

    private Object readZIPFile(Path cacheFile){
        Path cacheFileName = Paths.get(cacheFile.toString() + ".zip");

        try (InputStream fileInputStream = Files.newInputStream(cacheFileName);
             ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {

            ZipEntry zipEntry = zipInputStream.getNextEntry();
            if (zipEntry == null) {
                throw new RuntimeException("ZIP-файл пустой: " + cacheFileName);
            }

            try (ObjectInputStream objectInputStream = new ObjectInputStream(zipInputStream)) {
                System.out.print("Взято из кэша - ");
                return objectInputStream.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении ZIP-файла: " + cacheFile, e);
        }
    }
}

