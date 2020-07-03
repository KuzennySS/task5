package controller;

import models.answer.FinalPriceReceipt;
import models.request.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import service.CalculateCart;

@RestController
public class GoodsRestController {

    private CalculateCart calculateCart;

    @Autowired
    public GoodsRestController(CalculateCart calculateCart) {
        this.calculateCart = calculateCart;
    }

    /**
     * Загрузка новой матрицы промо-механик
     */
    @PostMapping("/promo")
    public ResponseEntity<?> getPromo(/*@RequestBody PromoMatrix body*/) {

        return new ResponseEntity<>("Правила успешно загружены", HttpStatus.OK);
    }

    /**
     * Расчитать стоимость позиций в чеке для указанной корзины
     */
    @PostMapping("/receipt")
    public ResponseEntity<?> getActualPrice(@RequestBody ShoppingCart cart) {
        final FinalPriceReceipt finalPriceReceipt = calculateCart.prepareAnswer(cart);

        return finalPriceReceipt != null
                ? new ResponseEntity<>(finalPriceReceipt, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
