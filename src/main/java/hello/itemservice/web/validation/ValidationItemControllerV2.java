package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;

    // Validator 등록용 ( addItemV5 메서드에서 사용 )
    private final ItemValidator itemValidator;

    // Validator 를 구현한 ItemValidator 를 WebDataBinder 에 등록한다 ( addItemV6 메서드에서 사용 )
    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        log.info("init WebDataBinder1={}", webDataBinder);
        webDataBinder.addValidators(itemValidator);
        log.info("init WebDataBinder2={}", webDataBinder);
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    /*
    BindingResult 의 첫 번째 적용 방식
    - ObjectError 의 최소 기능만 사용 ( 거절값 설정 없음, 메시지 코드 설정 없음 )
    - FieldError 의 최소 기능만 사용 ( 거절값 설정 없음, 메시지 코드 설정 없음 )
     */
    //@PostMapping("/add")
    public String addItemV1(
            @ModelAttribute Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // 0. BindingResult 를 이용한 입력값 검증
        // BindingResult 는 자동으로 View 에 전달된다. Model 이 없어도 된다
        // BindingResult 는 입력값을 검증하려는 @ModelAttribute 적용 객체 바로 다음에 위치해야 한다
        // FieldError 와 ObjectError 를 사용
        // FieldError 가 BindingResult 에 Add 되면 Thymeleaf 의 th:field 는 자신이 바인딩 한 해당 객체.속성과 동일한 객체.속성을 가지는 FieldError 객체의 메시지를 찾아서 출력해준다
        // 거절 값을 설정하지 않는 방식 적용 - FieldError 가 발생한 객체.필드는 입력했던 값(거절된 값)이 null 로 설정되므로, Thymeleaf 에서 입력했던 값을 출력할 수 없음
        // 메시지 코드를 사용하지 않는 방식 적용

        // 0. 입력값 검증 기초자료
        log.info("bindingResult.getClass()={}", bindingResult.getClass());

        String itemName = item.getItemName();
        Integer price = item.getPrice();
        Integer quantity = item.getQuantity();

        // 1. 상품명
        if (!StringUtils.hasText(itemName)) {
            FieldError fieldError = new FieldError("item", "itemName", "상품명은 필수입력입니다.");
            bindingResult.addError(fieldError);
        }

        // 2. 가격
        if (price == null || price < 1000 || price > 1000000) {
            FieldError fieldError = new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
            bindingResult.addError(fieldError);
        }

        // 3. 수량
        if (quantity == null || quantity > 9999) {
            FieldError fieldError = new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다.");
            bindingResult.addError(fieldError);
        }

        // 4. 가격 * 수량
        if (price != null && quantity != null) {
            int resultPrice = price * quantity;
            if (resultPrice < 10000) {
                ObjectError objectError = new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);
                bindingResult.addError(objectError);
            }
        }

        // 5. 입력값 오류시 입력 폼 화면을 다시 렌더링해서 클라이언트에게 전달
        if (bindingResult.hasErrors()) {
            log.info("bindingResult={}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /*
    BindingResult 의 두 번째 적용 방식
    - ObjectError 의 거절값 기능 사용 ( 메시지 코드 설정 없음 )
    - FieldError 의 거절값 기능 사용 ( 메시지 코드 설정 없음 )
     */
    //@PostMapping("/add")
    public String addItemV2(
            @ModelAttribute Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // 0. 검증 준비
        String itemName = item.getItemName();
        Integer price = item.getPrice();
        Integer quantity = item.getQuantity();

        // 상품명
        if (!StringUtils.hasText(itemName)) {
            FieldError fieldError = new FieldError("item", "itemName", "상품명은 필수입력입니다.");
            bindingResult.addError(fieldError);
        }
        // 가격
        if (price == null || price < 1000 || price > 1000000) {
            FieldError fieldError = new FieldError("item", "price", price, false, null, null, "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
            bindingResult.addError(fieldError);
        }
        // 수량
        if (quantity == null || quantity > 9999) {
            FieldError fieldError = new FieldError("item", "quantity", quantity, false, null, null, "수량은 최대 9,999 까지 허용합니다.");
            bindingResult.addError(fieldError);
        }
        // 가격 * 수량
        if (price != null && quantity != null) {
            int resultPrice = price * quantity;
            if (resultPrice < 10000) {
                ObjectError objectError = new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);
                bindingResult.addError(objectError);
            }
        }

        // 입력값 검증 오류가 있는 경우, 입력폼으로 돌아감
        if (bindingResult.hasErrors()) {
            log.info("addItemV2.bindingResult={}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /*
    BindingResult 의 세 번째 적용 방식
    - ObjectError 의 거절값 및 메시지 코드 기능 사용
    - FieldError 의 거절값 및 메시지 코드 기능 사용
     */
    //@PostMapping("/add")
    public String addItemV3(
            @ModelAttribute Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // 입력값 검증 준비
        String itemName = item.getItemName();
        Integer price = item.getPrice();
        Integer quantity = item.getQuantity();
        // 상품명 검증
        if (!StringUtils.hasText(itemName)) {
            FieldError fieldError = new FieldError("item", "itemName", itemName, false, new String[]{"required.item.itemName"}, null, null);
            bindingResult.addError(fieldError);
        }
        // 가격 검증
        if (price == null || price < 1000 || price > 1000000) {
            FieldError fieldError = new FieldError("item", "price", price, false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null);
            bindingResult.addError(fieldError);
        }
        // 수량 검증
        if (quantity == null || quantity > 9999) {
            FieldError fieldError = new FieldError("item", "quantity", quantity, false, new String[]{"max.item.quantity"}, new Object[]{9999}, null);
            bindingResult.addError(fieldError);
        }
        // 가격 * 수량 검증
        if (price != null && quantity != null) {
            int resultPrice = price * quantity;
            if (resultPrice < 10000) {
                ObjectError objectError = new ObjectError("item", new String[]{"totalPrice"}, new Object[]{10000, resultPrice}, null);
                bindingResult.addError(objectError);
            }
        }

        // 입력값 오류가 있는 경우 입력 폼으로 되돌아가기
        if (bindingResult.hasErrors()) {
            log.info("addItemV3.bindingResult={}", bindingResult);
            return "validation/v2/addForm";
        }

        // 입력값 오류가 없는 경우 업무 진행 및 View 전달
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /*
    BindingResult 의 네 번째 적용 방식
    - ObjectError 대신 BindingResult.reject() 사용
      -> ObjectError 생성
         Object (@ModelAttribute 설정 객체) 자동 지정
         오류 메시지는 메시지 코드 리졸버가 코드를 자동 생성하며, 메시지 properties 파일들에서 일치하는 코드를 찾아서 메시지를 설정함

    - FieldError 대신 BindingResult.rejectValue() 사용
      -> FieldError 생성
         Object (@ModelAttribute 설정 객체) 자동 지정
         오류가 발생한 Field 만 지정하면 됨
         오류 메시지는 메시지 코드 리졸버가 코드를 자동 생성하며, 메시지 properties 파일들에서 일치하는 코드를 찾아서 메시지를 설정함
         rejectValue 도 자동 지정

    - 메시지 코드 리졸버가 오류 코드를 자동생성하는 규칙
        -> 오류코드, 오브젝트가 입력된 경우
        오류코드.오브젝트
        오류코드
        -> 오류코드, 오브젝트, 필드, 클래스타입이 입력된 경우
        오류코드.오브젝트.필드
        오류코드.필드
        오류코드.클래스타입
        오류코드
     */
    //@PostMapping("/add")
    public String addItemV4(
            @ModelAttribute Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {

        // 입력값 검증 준비
        String itemName = item.getItemName();
        Integer price = item.getPrice();
        Integer quantity = item.getQuantity();
        String email = item.getEmail();

        // 상품명 검증
        if (!StringUtils.hasText(itemName)) {
            bindingResult.rejectValue("itemName", "required");
        }

        // 가격 검증
        if (price == null || price < 1000 || price > 1000000) {
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }

        // 수량 검증
        if (quantity == null || quantity > 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 이메일 검증
        if (!StringUtils.hasText(email)) {
            bindingResult.rejectValue("email", "required");
        } else {
            if (!Pattern.matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$", email)) {
                bindingResult.rejectValue("email", "incorrect");
            }
        }

        // 가격 * 수량 검증
        if (price != null && quantity != null) {
            int resultPrice = price * quantity;
            if (resultPrice < 10000) {
                bindingResult.reject("totalPrice", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("addItemV4 - bindingResult.getObjectName()={}", bindingResult.getObjectName());
            log.info("addItemV4 - bindingResult.getTarget()={}", bindingResult.getTarget());
            log.info("addItemV4 - bindingResult={}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /*
    BindingResult 의 다섯 번째 적용 방식
    - 별도의 검증용 클래스를 만들어서 직접 호출해 사용해본다 ( 어노테이션 사용 없이 직접 스프링 컨테이너에서 스프링 빈을 가져와서 사용해본다 )
    - 별도의 검증용 클래스는 아래 처럼 구현
      - implements Validator 하여 supports 와 validate 구현
      - validate 에서는 검증 대상 객체와 검증 결과를 담을 BindingResult 객체 필요
     */
    //@PostMapping("/add")
    public String addItemV5(
            @ModelAttribute Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (itemValidator.supports(item.getClass())) {
            itemValidator.validate(item, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            log.info("addItemV5 - bindingResult={}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    /*
    BindingResult 의 여섯 번째 적용 방식
    - 별도의 검증용 클래스를 만들고, 웹 데이터 바인더에 등록 ( @InitBinder, WebDataBinder, WebDataBinder.addValidator() ) - 클래스 내에 별도 메서드로 정의 필요
    - @ModelAttribute 대상 객체 앞에 @Validate 를 붙혀서 검증을 실행
    - 그에 대한 결과가 BindingResult 에 자동으로 들어간다
     */
    @PostMapping("/add")
    public String addItemV6(
            @Validated @ModelAttribute Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            log.info("addItemV6 - bindingResult={}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

