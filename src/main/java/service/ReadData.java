package service;

import models.Group;
import models.Item;

import java.util.List;

public interface ReadData {

    Item getByIdItem(String id);

    List<Item> getAllItem();

    Group getByIdGroup(String id);

    List<Group> getAllGroup();
}
