package com.sains.store;

import com.sain.annotation.Factory;

@Factory(id = "Margherita", type = Meal.class)
public class MargheritaPizza implements Meal{
    @Override
    public float getPrice() {
        return 6f;
    }
}
