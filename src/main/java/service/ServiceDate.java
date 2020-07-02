package service;

import models.*;
import models.dto.Positions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Расчет стоимости всего заказа с учетом скидки
     * @param bodyRequestReceipt    -   входной заказ
     * @return                      -   рассчитанный заказ
     */
    public BodyAnswerReceipt prepareAnswer(BodyRequestReceipt bodyRequestReceipt){
        BigDecimal percent = getPercentDiscount(bodyRequestReceipt.getShopId(), bodyRequestReceipt.getLoyaltyCard());
        List<Position> positions = bodyRequestReceipt.getPositions();
        List<Positions> positionDtos = positions.stream()
                .map(position -> createPositionDto(position, percent))
                .collect(Collectors.toList());
        BigDecimal sum = positions.stream()
                .map(this::getSum)
                .reduce(BigDecimal::add)
                .get();
        return BodyAnswerReceipt.builder()
                .total(sum.multiply(new BigDecimal(1).subtract(percent)))
                .discount(percent)
                .positions(positionDtos)
                .build();
    }

    /**
     * Подготовка PositionDto
     * @param position  -   позиция товара
     * @param percent   -   процент скидки
     * @return          -   PositionDto
     */
    private Positions createPositionDto(Position position, BigDecimal percent) {
        Item item = getByIdItem(position.getItemId());
        return Positions.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice().multiply(new BigDecimal(1).subtract(percent)))
                .regularPrice(item.getPrice())
                .build();
    }

    /**
     * Расчет скидки исходя из акций магазина и наличия дисконтной карты
     * @param shopId        -   номер магазина
     * @param loyaltyCard   -   наличие дисконтной карты
     * @return              -   процент скидки
     */
    private BigDecimal getPercentDiscount(String shopId, boolean loyaltyCard){
        return loyaltyCard ? new BigDecimal("0.05") : new BigDecimal(0);
    }

    /**
     * Расчет суммы по одному товару
     * @param position  -   одна из позиций в чеке
     * @return          -   сумма по одной позиции
     */
    private BigDecimal getSum(Position position){
        Item item = getByIdItem(position.getItemId());
        return item.getPrice().multiply(new BigDecimal(position.getQuantity()));

    }

}
