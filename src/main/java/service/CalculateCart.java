package service;

import models.answer.FinalPriceReceipt;
import models.request.ShoppingCart;
import models.Item;
import models.request.ItemPosition;
import models.answer.FinalPricePosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис расчета скидок и суммы чека с учетом скидок
 */
@Service
public class CalculateCart {

    private final ServiceDate serviceDate;

    @Autowired
    public CalculateCart(ServiceDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    /**
     * Расчет стоимости всего заказа с учетом скидки
     * @param shoppingCart    -   входной заказ
     * @return                      -   рассчитанный заказ
     */
    public FinalPriceReceipt prepareAnswer(ShoppingCart shoppingCart){
        BigDecimal percent = getPercentDiscount(String.valueOf(shoppingCart.getShopId()), shoppingCart.getLoyaltyCard());
        List<ItemPosition> itemPositions = shoppingCart.getPositions();
        List<FinalPricePosition> positionDtos = itemPositions.stream()
                .map(itemPosition -> createPositionDto(itemPosition, percent))
                .collect(Collectors.toList());
        BigDecimal sum = itemPositions.stream()
                .map(this::getSum)
                .reduce(BigDecimal::add)
                .get();
        return FinalPriceReceipt.builder()
                .total(sum.multiply(new BigDecimal(1).subtract(percent)).setScale(2, RoundingMode.HALF_EVEN))
                .discount(percent.multiply(sum).setScale(2, RoundingMode.HALF_EVEN))
                .positions(positionDtos)
                .build();
    }

    /**
     * Подготовка PositionDto
     * @param itemPosition  -   позиция товара
     * @param percent   -   процент скидки
     * @return          -   PositionDto
     */
    private FinalPricePosition createPositionDto(ItemPosition itemPosition, BigDecimal percent) {
        Item item = serviceDate.getByIdItem(itemPosition.getItemId());
        return FinalPricePosition.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice().multiply(new BigDecimal(1).subtract(percent)).setScale(2, RoundingMode.HALF_EVEN))
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
     * @param itemPosition  -   одна из позиций в чеке
     * @return          -   сумма по одной позиции
     */
    private BigDecimal getSum(ItemPosition itemPosition){
        Item item = serviceDate.getByIdItem(itemPosition.getItemId());
        return item.getPrice().multiply(new BigDecimal(itemPosition.getQuantity()));

    }
}
