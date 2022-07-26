package hello.itemservice.domain.item;

import lombok.Data;

@Data
// ObjectError 에 대한 검증
//@ScriptAssert(lang = "javascript", script = "_this.price * this.quantity >= 10000")
public class Item {

    //@NotNull(groups = {UpdateCheck.class})
    private Long id;
    //@NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
    private String itemName;
    //@NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    //@Range(min = 1000, max = 1000000, groups = {SaveCheck.class})
    private Integer price;
    //@NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    //@Max(value = 9999, groups = {SaveCheck.class})
    private Integer quantity;

    //@Email(groups = {SaveCheck.class, UpdateCheck.class})
    private String email;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
