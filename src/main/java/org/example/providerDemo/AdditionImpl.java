package org.example.providerDemo;

import org.example.services.Addition;

public class AdditionImpl implements Addition {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
