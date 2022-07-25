package hello.itemservice.validation;

import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

import java.util.Arrays;

public class MessageCodesResolverTest {

    // 메시지 코드 리졸버
    MessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();

    @Test
    void 객체명입력_메시지코드리졸버_코드생성() {
        // 객체명을 입력하고 에러코드를 지정한 경우, 메시지 코드 리졸버가 자동 생성해주는 메시지 코드 목록들을 확인하자
        String[] messageCodes = messageCodesResolver.resolveMessageCodes("required", "item");
        System.out.println(Arrays.toString(messageCodes));
    }

    @Test
    void 객체명_필드명입력_메시지코드리졸버_코드생성() {
        String[] messageCodes = messageCodesResolver.resolveMessageCodes("required", "item", "itemName", null);
        System.out.println(Arrays.toString(messageCodes));
    }

    @Test
    void 객체명_필드명_타입입력_메시지코드리졸버_코드생성() {
        String[] messageCodes = messageCodesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
        System.out.println(Arrays.toString(messageCodes));
    }

}
