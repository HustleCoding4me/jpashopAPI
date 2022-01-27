package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable//jpa의 내장타입이란 뜻
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    /*
    Setter를 제거하고, 생성자에서 값읆 모두 초기화해서 변경 불가능하게 만들고,
    JPA 스펙상 임베디드타입, 엔티티는 자바 기본 생성자를 protected로 설정해야한다.
    JPA 구현 라이브러리가 객체를 생성할 때 리플렉션 같은 기술을 사용할 수 있또록 지원해야하기 때문,
     */
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
