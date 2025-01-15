package main.java.org.example.Impl;



import main.java.org.example.Service.Service;

import java.util.List;

// Класс создан, чтобы показать, что если в прокси обернут другой имплементатор моего интерфейса, то он будет создавать под него эксклюзивные кэш файлы
public class ServiceImpl2 implements Service {
    @Override
    public int run(String item, int value) {
        return value * 2;
    }

    @Override
    public List<String> work(String item) {
        return null;
    }

    @Override
    public int workIntoFile(int a) {
        return 0;
    }
}
