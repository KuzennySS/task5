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
        // расчет суммы без скидки
        BigDecimal sum = itemPositions.stream()
                .map(this::getSum)
                .reduce(BigDecimal::add)
                .get();

        // сумма скидки по акциям
        BigDecimal sumDiscount;
        // процент скидки по акции
        BigDecimal percentDiscount = BigDecimal.valueOf(0);
        // Проверка наличия скидочных акций
        if (promoMatrix.getItemCountRules() != null || promoMatrix.getLoyaltyCardRules() != null || promoMatrix.getItemGroupRules() != null){
            // Получение процента скидки по Промо-акции Карта Лояльности
            BigDecimal percentLoyalityCard = getProcentLoyalityCardDiscount(shoppingCart);
            BigDecimal sumDiscountLoyalityCard = sum.multiply(percentLoyalityCard);
            // Получение скидки по Промо-акция N+k
            BigDecimal sumDiscountNaddK = getSumDiscountDiscountNaddK(shoppingCart);
            // Получение скидки по Промо-акция Скидка на товарную группу
            BigDecimal sumDiscountItemGroup = getSumDiscountItemGroupDiscount(shoppingCart);
            // сравнить действующие скидки и получить максимальную
            if (sumDiscountLoyalityCard.compareTo(sumDiscountNaddK) >= 0 && sumDiscountLoyalityCard.compareTo(sumDiscountItemGroup) >= 0){
                sumDiscount = sumDiscountLoyalityCard.setScale(2, RoundingMode.HALF_EVEN);
                percentDiscount = percentLoyalityCard;
            } else if (sumDiscountNaddK.compareTo(sumDiscountLoyalityCard) >= 0 && sumDiscountNaddK.compareTo(sumDiscountItemGroup) >= 0 ){
                sumDiscount = sumDiscountNaddK.setScale(2, RoundingMode.HALF_EVEN);
            } else {
                sumDiscount = sumDiscountItemGroup;
            }
        }
        else {
            sumDiscount = new BigDecimal("0.00");
        }

        // позиции в чеке с полной ценой и со скидкой
        BigDecimal finalPercentDiscount = percentDiscount;
        List<FinalPricePosition> positionDtos = itemPositions.stream()
                .map(itemPosition -> createPositionDto(itemPosition, finalPercentDiscount))
                .collect(Collectors.toList());

        // итоговая сумма
        BigDecimal sumResult = sum.subtract(sumDiscount).setScale(2, RoundingMode.HALF_EVEN);

        return FinalPriceReceipt.builder()
                .total(sumResult)
                .discount(sumDiscount)
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
     * Расчет процента скидки исходя из акций магазина и наличия дисконтной карты
     * @param cart                  -   входной заказ
     * @return percentLoyaltyCard   -   процент скидка по акции LoyalityCardD
     */
    private BigDecimal getProcentLoyalityCardDiscount(ShoppingCart cart) {
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
        return percentLoyaltyCard;
    }

    /**
     * Рассчет суммы скикди исходя из акцию "N+k"
     * @param basket                -   входной заказ
     * @return sumDiscount          -   сумма скидки по акции "N+k"
     */
    private BigDecimal getSumDiscountDiscountNaddK(ShoppingCart basket) {
        // включить проверку на акцию "N+k"
        ActionNaddK actionNaddK = getNaddKdiscount(basket);
        Integer bonusMatrixCount = actionNaddK.getBonusQuantity();
        BigDecimal sumDiscount = BigDecimal.valueOf(0);
        if (bonusMatrixCount != 0) {
            Integer positionWithBonus = Integer.parseInt(basket.getPositions().stream()
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
        }
        return sumDiscount;
    }

    /**
     * Расчет скидки исходя из акций магазина и наличия дисконтной карты
     * @param cart                  -   входной заказ
     * @return percentItemGroup     -   сумму скидки по акции LoyalityCardD
     */
    private BigDecimal getSumDiscountItemGroupDiscount(ShoppingCart cart) {
        // Проверяем акции при покупке связанных товаров
        BigDecimal percentItemGroup = BigDecimal.valueOf(0);
        Optional<BigDecimal> percentItemGroupOpt = promoMatrix.getItemGroupRules().stream()
                .map(matrixCart -> checkItemGroupRules(matrixCart, cart))
                .findAny();
        if (percentItemGroupOpt.isPresent()) {
            percentItemGroup = percentItemGroupOpt.get();
        }
        return percentItemGroup;
    }

    /**
     * Расчет скидки исходя из акций Промо-акция N+k
     * @param cart                  -   входной заказ
     * @return percentItemGroup     -   сумму скидки по акции LoyalityCardD
     */
    private ActionNaddK getNaddKdiscount(ShoppingCart cart) {
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
     * Проверяет корзину на наличие в ней всех акционных товаров для групповой скидки
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
                        .reduce(BigDecimal::add)
                        .map(sum -> sum.multiply(BigDecimal.valueOf(matrixCart.getDiscount())))
                        .get();

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
