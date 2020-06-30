package service;

import models.Group;
import models.Item;

import java.util.List;

public interface LoadData {

    List<Item> getAllItems();

    List<Group> getAllGroup();
}
