package service;

import models.Group;
import models.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class ServiceDate implements ReadData {

    private final DownloadDate downloadDate;

    @Autowired
    public ServiceDate(DownloadDate downloadDate) {
        this.downloadDate = downloadDate;
    }

    public static Map<String, Item> items;
    public static Map<String, Group> groups;

    @PostConstruct
    private void postConstruct() {
        items = downloadDate.getAllItems();
        groups = downloadDate.getAllGroup();
    }

    @Override
    public Item getByIdItem(String id) {
        return items.get(id);
    }

    @Override
    public List<Item> getAllItem() {
        return null;
    }

    @Override
    public Group getByIdGroup(String id) {
        return groups.get(id);
    }

    @Override
    public List<Group> getAllGroup() {
        return null;
    }
}
