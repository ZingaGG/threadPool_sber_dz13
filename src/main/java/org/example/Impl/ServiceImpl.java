package org.example.Impl;

import org.example.Service.Service;

import java.util.ArrayList;
import java.util.List;

public class ServiceImpl implements Service {

    @Override
    public int run(String item, int value) {
        return value * 2;
    }

    @Override
    public List<String> work(String item) {
        List<String> res = new ArrayList<>();

        // 21 - число, которое превышает капасити, определенного в интерфейсе Service
        for (int i = 1; i < 21; i++) {
            res.add(item + " " + i);
        }

        return res;
    }

    @Override
    public int workIntoFile(int a) {
        return a;
    }
}
