package service;

import models.Group;
import models.Item;

import java.util.Map;

public interface LoadData {

    Map<String, Item> getAllItems();

    Map<String, Group> getAllGroup();
}
