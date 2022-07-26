package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }

    @PostMapping("/add")
    public String addItem(
            @Validated @ModelAttribute(name = "item") ItemSaveForm itemSaveForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
            ) {

        // ObjectError 검증
        if (itemSaveForm.getPrice() != null && itemSaveForm.getQuantity() != null) {
            int resultPrice = itemSaveForm.getPrice() * itemSaveForm.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPrice", new Object[]{10000}, null);
            }
        }

        // BindingResult 에 오류가 있으면 입력 화면을 다시 클라이언트에게 전달
        if (bindingResult.hasErrors()) {
            log.info("addItem - bindingResult={}", bindingResult);
            return "validation/v4/addForm";
        }

        // BindingResult 에 오류가 없으면 저장 후 상세 화면을 클라이언트에게 전달
        Item item = new Item();
        item.setItemName(itemSaveForm.getItemName());
        item.setPrice(itemSaveForm.getPrice());
        item.setQuantity(itemSaveForm.getQuantity());
        item.setEmail(itemSaveForm.getEmail());

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(
            @PathVariable Long itemId,
            @Validated @ModelAttribute(name = "item") ItemUpdateForm itemUpdateForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // ObjectError 검증
        if (itemUpdateForm.getPrice() != null && itemUpdateForm.getQuantity() != null) {
            long resultPrice = itemUpdateForm.getPrice() * itemUpdateForm.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPrice", new Object[]{10000, resultPrice}, null);
            }
        }

        // BindingResult 에 오류가 있으면 수정 화면을 다시 보여줌
        if (bindingResult.hasErrors()) {
            log.info("edit - bindingResult={}", bindingResult);
            return "validation/v4/editForm";
        }

        // BindingResult 에 오류가 없으면 수정을 진행 한 후, 상세 화면으로 redirect

        Item item = new Item();
        item.setId(itemUpdateForm.getId());
        item.setItemName(itemUpdateForm.getItemName());
        item.setPrice(itemUpdateForm.getPrice());
        item.setQuantity(itemUpdateForm.getQuantity());
        item.setEmail(itemUpdateForm.getEmail());

        log.info("item={}", item);

        itemRepository.update(item.getId(), item);
        redirectAttributes.addAttribute("itemId", item.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }



}