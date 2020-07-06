package service;

import models.Item;
import models.answer.FinalPricePosition;
import models.answer.FinalPriceReceipt;
import models.dto.DiscountDto;
import models.request.ItemCountRules;
import models.request.ItemGroupRules;
import models.request.ItemPosition;
import models.request.ShoppingCart;
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
     *
     * @param shoppingCart -   входной заказ
     * @return -   рассчитанный заказ
     */
    public FinalPriceReceipt prepareAnswer(ShoppingCart shoppingCart) {
        List<ItemPosition> itemPositions = shoppingCart.getPositions();
        BigDecimal percent = getPercentDiscount(shoppingCart);
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
     *
     * @param itemPosition -   позиция товара
     * @param percent      -   процент скидки
     * @return -   PositionDto
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
     *
     * @param cart -   входной заказ
     * @return -   процент скидки
     */
    private BigDecimal getPercentDiscount(ShoppingCart cart) {
        if (promoMatrix == null) return new BigDecimal("0.00");

        // Проверяем акции при покупке связанных товаров
        BigDecimal percentItemGroup = promoMatrix.getItemGroupRules().stream()
                .map(matrixCart -> checkItemGroupRules(matrixCart, cart))
                .findAny().get();
        return percentItemGroup;


        // Проверяем акции при предъявлении пенсионного удостоверения или социальной карты

        // Определяем, какая акция выгоднее для покупателя












////        String shopId = String.valueOf(shoppingCart.getShopId());
////        boolean loyaltyCard = shoppingCart.getLoyaltyCard();
//        shoppingCart.getPositions().stream()
//                .filter(this::checkCountRules)
//                .
//        // определяем какие скидки есть вообще и выбираем по приоритету
//        if (promoMatrix.getItemCountRules() != null) {
//
//            BigDecimal percentCountRules =
//        }
//
////        Если для позиции в чеке может применяться сразу несколько промо-акций, то выбирается промо-акция с наиболее высоким
////        приоритетом.
////        Если для позиции чека подходят несколько промо-акций с одинаковым приоритетом, то нужно применять ту промо-акцию,
////        которая даёт покупателю наибольшую скидку.
//

        // если есть в наличии скидка по предъявлению пенсионного
//        if (loyaltyCard && promoMatrix.getLoyaltyCardRules() != null)
//            return BigDecimal.valueOf(promoMatrix.getLoyaltyCardRules().get(0).getDiscount());
//        return new BigDecimal("0.01");
//        return loyaltyCard ? new BigDecimal("0.05") : new BigDecimal(0);
    }

    /**
     * Проверяет корзину на наличие в ней всех акционных товаров
     * @param matrixCart    -   группа товаров со скидкой
     * @param shoppingCart  -   корзина покупателя
     * @return              -   скидка
     */
    private BigDecimal checkItemGroupRules(ItemGroupRules matrixCart, ShoppingCart shoppingCart){
        // проверяем на совпадения id магазина
        if (matrixCart.getShopId().equals(shoppingCart.getShopId())){
            // получить какая группа акционных товаров должна быть
            List<String> actionGroups = matrixCart.getGroupId();
            // получить список групп для товаров из корзины
            List<String> cartGroups = shoppingCart.getPositions().stream()
                    .map(cart -> serviceDate.getByIdItem(cart.getItemId()).getGroupId())
                    .collect(Collectors.toList());
            // проверить вхождение этой группы в корзину
            long containCart = actionGroups.stream()
                    .filter(cartGroups::contains)
                    .distinct()
                    .count();
            if (containCart == actionGroups.size()) return BigDecimal.valueOf(matrixCart.getDiscount());
        }
        return BigDecimal.valueOf(0);
    }

    /**
     * Проходит по всему списку акций и проверяет наличие акционных товаров в корзине
     * @param itemPosition  -   позиция, которая проверяется на наличие акций на нее
     * @return              -   true акция выполняется, false не выполняется
     */
    private Boolean checkCountRules(ItemPosition itemPosition) {
        return promoMatrix.getItemCountRules().stream()
                .anyMatch(promoMatrix -> checkRule(itemPosition, promoMatrix));
    }

    /**
     * Проверяет выполнение условий акции для конкретной позиции в списке
     * @param itemPosition  -   позиция, которая проверяется на наличие акций на нее
     * @param promoMatrix   -   одно из акционных условий
     * @return              -   true акция выполняется, false не выполняется
     */
    private boolean checkRule(ItemPosition itemPosition, ItemCountRules promoMatrix) {
        return promoMatrix.getItemId().equals(itemPosition.getItemId())
                && promoMatrix.getBonusQuantity() <= Integer.parseInt(itemPosition.getItemId());
    }

    /**
     * Расчет суммы по одному товару
     *
     * @param itemPosition -   одна из позиций в чеке
     * @return -   сумма по одной позиции
     */
    private BigDecimal getSum(ItemPosition itemPosition) {
        Item item = serviceDate.getByIdItem(itemPosition.getItemId());
        return item.getPrice().multiply(new BigDecimal(itemPosition.getQuantity()));

    }

}
