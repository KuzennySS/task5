package service;

import models.Group;
import models.Item;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class DownloadDate implements LoadData{

    BufferedReader reader;

    {
        try {
            reader = new BufferedReader(new FileReader(
                        "csv\\groups.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<Item> getAllItems() {
        return null;
    }

    @Override
    public List<Group> getAllGroup() {
        return null;
    }
}
