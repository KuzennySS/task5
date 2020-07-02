package controller;

import models.BodyAnswerReceipt;
import models.BodyRequestReceipt;
import models.Group;
import models.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import service.ServiceDate;

@RestController
public class GoodsRestController {

    private ServiceDate serviceDate;

    @Autowired
    public GoodsRestController(ServiceDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    /**
     * в теле POST запроса приходит список дескрипторов актуальных промо-акции
     */
    @PostMapping("/promo")
    public ResponseEntity<?> getPromo(/*@PathVariable(name = "id") int id*/) {
        final Item items = serviceDate.getByIdItem("3636974");

        return items != null
                ? new ResponseEntity<>(items, HttpStatus.OK)
                : new ResponseEntity<>(/*new ErrorRequest("branch not found"),*/ HttpStatus.NOT_FOUND);
    }

    /**
     * список товаров с корректными ценами и названиями, размером скидки, сумма чека и суммарный размер скидки
     */
    @PostMapping("/receipt")
    public ResponseEntity<?> getActualPrice(@RequestBody BodyRequestReceipt bodyRequestReceipt) {
        final BodyAnswerReceipt bodyAnswerReceipt = serviceDate.prepareAnswer(bodyRequestReceipt);

        return bodyAnswerReceipt != null
                ? new ResponseEntity<>(bodyAnswerReceipt, HttpStatus.OK)
                : new ResponseEntity<>(/*new ErrorRequest("branch not found"),*/ HttpStatus.NOT_FOUND);
    }
}
