package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        // 이 Validator 가 Item 클래스에 대해 검증을 할 수 있는지 알려준다
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        // supports 를 통해 Item 만 이 Validator 를 쓸 수 있게 했으므로, Item 으로 형변환해도 오류가 발생하지 않는다.
        Item item = (Item) target;
        String itemName = item.getItemName();
        Integer price = item.getPrice();
        Integer quantity = item.getQuantity();
        String email = item.getEmail();
        
        // 상품이름
        if (!StringUtils.hasText(itemName)) {
            errors.rejectValue("itemName", "required");
        }
        // 가격
        if (price == null || price < 1000 || price > 1000000) {
            errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        
        // 수량
        if (quantity == null || quantity > 9999) {
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 이메일
        if (!StringUtils.hasText(email)) {
            errors.rejectValue("email", "required");
        } else {
            if (!Pattern.matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$", email)) {
                errors.rejectValue("email", "incorrect");
            }
        }

        // 가격 * 수량
        if (price != null && quantity != null) {
            int resultPrice = price * quantity;
            if (resultPrice < 10000) {
                errors.reject("totalPrice", new Object[]{10000, resultPrice}, null);
            }
        }

    }
}
