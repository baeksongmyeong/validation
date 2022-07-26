package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
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

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }

    /*
    BindingResult 와 Bean Validator 의 첫 번째 적용 방식
    - Bean Validator 라이브러리 추가
    - 이렇게 하면 스프링이 자동으로 Bean Validator 의 구현체를 등록
    - @Validated 로 Bean Validator 를 호출 가능
    - ObjectError 는 @ScriptAssert 로 처리할 수 있지만 제약이 크므로, 컨트롤러에서 직접 ObjectError 를 생성하는게 좋다.
    - 상품 저장과 상품 수정의 Bean Validation 시 조건이 다를 수 있는데 이때는 어노테이션의 group 속성을 사용한다
    - 먼저 상품 저장, 상품 수정을 각각 나타내는 비어있는 인터페이스를 구분자 개념으로 생성하고
    - Bean Validation 용 어노테이션의 group 속성에 이 인터페이스들을 용도에 맞게 넣는다
    - 그리고 컨트롤러에서 @Validated 시 인자로 이 인터페이스를 넣어주면
    - Bean Validator 는 객체를 검증할 때, group 에 있는 인터페이스 목록과 @Validated 가 제공한 인터페이스가 일치하는 검증 요소만 수행한다
     */
    @PostMapping("/add")
    public String addItem(
            @Validated(SaveCheck.class) @ModelAttribute Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // ObjectError 는 @ScriptAssert 를 사용하기 보다는 컨트롤러에서 직접 ObjectError 를 생성하는 것이 편한다.
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPrice", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("addItem - bindingResult={}", bindingResult);
            return "validation/v3/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(
            @PathVariable Long itemId,
            @Validated(UpdateCheck.class) @ModelAttribute Item item,
            BindingResult bindingResult
    ) {
        // ObjectError 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPrice", new Object[]{10000, resultPrice}, null);
            }
        }
        
        // FieldError, ObjectError 가 있는 경우, 요청을 한 화면을 다시 렌더링해서 보내줌
        if (bindingResult.hasErrors()) {
            log.info("edit - bindingResult={}", bindingResult);
            return "validation/v3/editForm";
        }
        
        // FieldError, ObjectError 가 없는 경우, 업무 로직을 수행함
        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

