package com.sains.store;

import java.lang.String;

class MealFactory {
  public Meal create(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id is null!");
    }
    if ("Margherita".equals(id)) {
      return new com.sains.store.MargheritaPizza();
    }
    if ("Calzone".equals(id)) {
      return new com.sains.store.CalzonePizza();
    }
    if ("Tiramisu".equals(id)) {
      return new com.sains.store.Tiramisu();
    }
    throw new IllegalArgumentException("Unknown id = " + id);
  }
}
