package service;

import models.Item;
import models.answer.FinalPricePosition;
import models.answer.FinalPriceReceipt;
import models.dto.ActionNaddK;
import models.request.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        BigDecimal discount = percent.multiply(sum).setScale(2, RoundingMode.HALF_EVEN);

        // включить проверку на акцию "N+k"
        ActionNaddK actionNaddK = getActionNaddK(shoppingCart);
        Integer bonusMatrixCount = actionNaddK.getBonusQuantity();
        BigDecimal sumDiscount = BigDecimal.valueOf(0);
        if (bonusMatrixCount != 0) {
            Integer positionWithBonus = Integer.parseInt(shoppingCart.getPositions().stream()
                    .filter(cart -> cart.getItemId().equals(actionNaddK.getIdPosition()))
                    .map(ItemPosition::getQuantity)
                    .findAny()
                    .get());
            int delta = positionWithBonus - actionNaddK.getTrigerQuantity();
            if (delta < bonusMatrixCount) {
                sumDiscount = serviceDate.getByIdItem(String.valueOf(actionNaddK.getIdPosition())).getPrice().multiply(BigDecimal.valueOf(delta));
            } else {
                sumDiscount = serviceDate.getByIdItem(String.valueOf(actionNaddK.getIdPosition())).getPrice().multiply((BigDecimal.valueOf(bonusMatrixCount)));
            }
            sum = sum.subtract(sumDiscount);
            discount = sumDiscount;
        }


        return FinalPriceReceipt.builder()
                .total(sum.multiply(new BigDecimal(1).subtract(percent)).setScale(2, RoundingMode.HALF_EVEN))
                .discount(discount)
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
        BigDecimal percentItemGroup = BigDecimal.valueOf(0);
        Optional<BigDecimal> percentItemGroupOpt = promoMatrix.getItemGroupRules().stream()
                .map(matrixCart -> checkItemGroupRules(matrixCart, cart))
                .findAny();
        if (percentItemGroupOpt.isPresent()) {
            percentItemGroup = percentItemGroupOpt.get();
        }

        // Проверка акций по карте лояльности
        BigDecimal percentLoyaltyCard = BigDecimal.valueOf(0);
        if (cart.getLoyaltyCard()) {
            Optional<LoyaltyCardRule> loyaltyCardRule = promoMatrix.getLoyaltyCardRules().stream()
                    .filter(item -> item.getShopId().equals(cart.getShopId()))
                    .findAny();
            if (loyaltyCardRule.isPresent()) {
                percentLoyaltyCard = BigDecimal.valueOf(loyaltyCardRule.get().getDiscount());
            }
        }

        // Определяем, какая акция выгоднее для покупателя
        if (percentItemGroup.compareTo(percentLoyaltyCard) >= 0) {
            return percentItemGroup;
        }
        return percentLoyaltyCard;
    }

    private ActionNaddK getActionNaddK(ShoppingCart cart) {
        // Проверка акции формата "N+k"
        String bonusPosition = null;
        int trigerQuantity = 0;
        int bonusQuantity = 0;
        List<ItemCountRules> itemCountRules = promoMatrix.getItemCountRules();
        if (itemCountRules.size() > 0) {
            Optional<ItemCountRules> discountItemCountRule = itemCountRules.stream()
                    .filter(itemCountRule -> checkItemCountRule(itemCountRule, cart))
                    .findAny();
            if (discountItemCountRule.isPresent()) {
                bonusQuantity = discountItemCountRule.get().getBonusQuantity();
                bonusPosition = discountItemCountRule.get().getItemId();
                trigerQuantity = discountItemCountRule.get().getTriggerQuantity();
            }
        }
        return ActionNaddK.builder()
                .idPosition(bonusPosition)
                .bonusQuantity(bonusQuantity)
                .trigerQuantity(trigerQuantity)
                .build();
    }

    private boolean checkItemCountRule(ItemCountRules itemCountRule, ShoppingCart carts) {
        if (itemCountRule.getShopId().equals(carts.getShopId())) {
            Optional<ItemPosition> itemPosition = carts.getPositions().stream()
                    .filter(cart -> cart.getItemId().equals(itemCountRule.getItemId()))
                    .findAny();
            return itemPosition.isPresent();
        }
        return false;
    }

    /**
     * Проверяет корзину на наличие в ней всех акционных товаров
     *
     * @param matrixCart   -   группа товаров со скидкой
     * @param shoppingCart -   корзина покупателя
     * @return -   скидка
     */
    private BigDecimal checkItemGroupRules(ItemGroupRules matrixCart, ShoppingCart shoppingCart) {
        BigDecimal result = BigDecimal.valueOf(0);
        // проверяем на совпадения id магазина
        if (matrixCart.getShopId().equals(shoppingCart.getShopId())) {
            // группа всех акционных товаров в этом магазине
            List<String> actionGroups = matrixCart.getGroupId();
            // группа для товаров из корзины
            Map<String, Integer> cartGroups = new HashMap<>();
            List<ItemPosition> positions = shoppingCart.getPositions();
            positions.forEach(cart -> cartGroups.put(getGrouId(cart), cartGroups.getOrDefault(getGrouId(cart), 0) + 1));
            // получаем группу товаров на которую надо считать скидку
            Optional<String> groupDiscount = actionGroups.stream()
                    .filter(group -> cartGroups.get(group) >= 2)
                    .findFirst();
            if (groupDiscount.isPresent()) {
                // получить все товары с ценой и количеством из группы на которую делаем скидку
                Map<String, Integer> itemDiscounts = new HashMap<>();
                positions.stream()
                        .filter(itemPosition -> getGrouId(itemPosition).equals(groupDiscount.get()))
                        .forEach(itemPosition -> itemDiscounts.put(itemPosition.getItemId(), itemDiscounts.getOrDefault(itemPosition.getItemId(), 0) + 1));
                // рассчитывам скидку, которую передим в конечный расчет
                result = itemDiscounts.keySet().stream()
                        .map(key -> BigDecimal.valueOf(itemDiscounts.get(key)).multiply(serviceDate.getByIdItem(key).getPrice()))
                        .reduce(BigDecimal::add).get();

            }
        }
        return result;
    }

    /**
     * Получаем ключ (группа товаров) для Map
     *
     * @param cart -   корзина
     * @return -   String ключ название группы
     */
    private String getGrouId(ItemPosition cart) {
        return serviceDate.getByIdItem(cart.getItemId()).getGroupId();
    }

    /**
     * Проходит по всему списку акций и проверяет наличие акционных товаров в корзине
     *
     * @param itemPosition -   позиция, которая проверяется на наличие акций на нее
     * @return -   true акция выполняется, false не выполняется
     */
    private Boolean checkCountRules(ItemPosition itemPosition) {
        return promoMatrix.getItemCountRules().stream()
                .anyMatch(promoMatrix -> checkRule(itemPosition, promoMatrix));
    }

    /**
     * Проверяет выполнение условий акции для конкретной позиции в списке
     *
     * @param itemPosition -   позиция, которая проверяется на наличие акций на нее
     * @param promoMatrix  -   одно из акционных условий
     * @return -   true акция выполняется, false не выполняется
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
