package service;

import models.Group;
import models.Item;

import java.util.List;

public interface ReadData {

    Item getByIdItem(Integer id);

    List<Item> getAllItem();

    Group getByIdGroup(Integer id);

    List<Group> getAllGroup();
}
