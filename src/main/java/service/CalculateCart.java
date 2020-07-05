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

import static controller.GoodsRestController.promoMatrix;

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
        if (promoMatrix == null) return new BigDecimal("0.00");
        // определяем какие скидки есть вообще
//        Если для позиции в чеке может применяться сразу несколько промо-акций, то выбирается промо-акция с наиболее высоким приоритетом.
//        Если для позиции чека подходят несколько промо-акций с одинаковым приоритетом, то нужно применять ту промо-акцию, которая даёт покупателю наибольшую скидку.


        // если есть в наличии скидка по предъявлению пенсионного
        if (loyaltyCard && promoMatrix.getLoyaltyCardRules() != null)
            return BigDecimal.valueOf(promoMatrix.getLoyaltyCardRules().get(0).getDiscount());
        return new BigDecimal("0.01");
//        return loyaltyCard ? new BigDecimal("0.05") : new BigDecimal(0);
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
