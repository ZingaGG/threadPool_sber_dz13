package org.example.Proxy.Utils;


import org.example.Proxy.CacheHandler;

import java.lang.reflect.Proxy;

public class CacheProxy {
    private final String rootPath;

    public CacheProxy(String rootPath){
        this.rootPath = rootPath;
    }

    @SuppressWarnings("unchecked")
    public <T> T cache(T target){
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new CacheHandler(rootPath, target));
    }
}
