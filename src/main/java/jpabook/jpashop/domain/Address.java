package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable//jpa의 내장타입이란 뜻
@Getter @Setter
public class Address {

    private String city;
    private String street;
    private String zipcode;

}
