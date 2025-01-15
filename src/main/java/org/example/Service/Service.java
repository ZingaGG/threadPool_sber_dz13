package main.java.org.example.Service;

import main.java.org.example.Annotation.Cache;
import main.java.org.example.Proxy.Utils.CacheType;

import java.util.List;

public interface Service {
    @Cache(cacheType = CacheType.FILE, fileNamePrefix = "data", zip = true, identityBy = {String.class}) // identityBy можно сменить на Integer.class для наглядности
    int run(String item, int value);

    @Cache(cacheType = CacheType.IN_MEMORY, listList = 10)
    List<String> work(String item);

    @Cache(cacheType = CacheType.FILE)
    int workIntoFile(int a);

}
