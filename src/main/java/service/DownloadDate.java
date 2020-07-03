package service;

import com.opencsv.CSVReader;
import models.Group;
import models.Item;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.math.BigDecimal;
import java.util.*;

/**
 *  Сервис загрузки данных из файлов csv
 */
@Service
public class DownloadDate implements LoadData {

    @Override
    public Map<String, Item> getAllItems() {
        Map<String, Item> items = new HashMap<>();
        try {
            //Build reader instance
            CSVReader reader = new CSVReader(new FileReader("D:\\study\\task5\\src\\main\\resources\\csv\\items.csv"));
            //Read all rows at once
            List<String[]> allRows = reader.readAll();
            //Read CSV line by line and use the string array as you want
            for (String[] row : allRows) {
                System.out.println(Arrays.toString(row));
                if (!"id".equals(row[0])) {  // пропускаем 1-ю строку
                    Item item = Item.builder()
                            .id(row[0])
                            .name(row[1])
                            .groupId(row[2])
                            .price(new BigDecimal(row[3]))
                            .build();
                    items.put(row[0],item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }


    @Override
    public Map<String, Group> getAllGroup() {
        Map<String, Group> groups = new HashMap<>();
        try {
            //Build reader instance
            CSVReader reader = new CSVReader(new FileReader("D:\\study\\task5\\src\\main\\resources\\csv\\groups.csv"));
            //Read all rows at once
            List<String[]> allRows = reader.readAll();
            //Read CSV line by line and use the string array as you want
            for (String[] row : allRows) {
                System.out.println(Arrays.toString(row));
                if (!"id".equals(row[0])) {  // пропускаем 1-ю строку
                    Group group = Group.builder()
                            .id(row[0])
                            .name(row[1])
                            .build();
                    groups.put(row[0],group);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groups;
    }
}
