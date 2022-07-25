package hello.itemservice.validation;

import hello.itemservice.domain.item.Item;
import org.apache.catalina.core.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@SpringBootTest
public class BeanValidationTest {

    @Test
    void 빈검증_스프링없이_테스트() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        Item item = new Item();
        item.setItemName("  ");
        item.setPrice(0);
        item.setQuantity(100000);
        item.setEmail("");

        Set<ConstraintViolation<Item>> validate = validator.validate(item);
        validate.stream().forEach(validation -> {
            System.out.println(validation);
            System.out.println(validation.getMessage());
        });
    }
}
