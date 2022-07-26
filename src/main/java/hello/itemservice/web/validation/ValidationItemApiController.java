package hello.itemservice.web.validation;

import hello.itemservice.web.validation.form.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/validation/api/items")
public class ValidationItemApiController {

    @PostMapping("/add")
    public Object addItem(
            @RequestBody @Validated ItemSaveForm itemSaveForm,
            BindingResult bindingResult
    ) {
        log.info("API 컨트롤러 호출");

        // @RequestBody -> HttpMessageConverter -> ItemSaveForm 으로의 바인딩시 오류가 발생하면 오류내역을 반환
        if (bindingResult.hasErrors()) {
            log.warn("검증 오류 발생 - bindingResult={}", bindingResult);
            return bindingResult.getAllErrors();
        }

        log.info("성공 로직 실행");
        return itemSaveForm;
    }
}
