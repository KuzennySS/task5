package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoodsRestController {

//    private AtmOfficeService atmOfficeService;
//
//    @Autowired
//    public GoodsRestController(AtmOfficeService atmOfficeService) {
//        this.atmOfficeService = atmOfficeService;
//    }

    /**
     * REST контроллер возвращает банкомат по id
     */
    @PostMapping("/promo")
    public ResponseEntity<?> getPromo(/*@PathVariable(name = "id") int id*/) {
//        final AtmOffice atmOffice = atmOfficeService.getById(id);

//        return atmOffice != null
//                ? new ResponseEntity<>(atmOffice, HttpStatus.OK)
//                : new ResponseEntity<>(new ErrorRequest("branch not found"), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>("All right", HttpStatus.OK);
    }
}
