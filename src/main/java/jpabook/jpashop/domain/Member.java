package jpabook.jpashop.domain;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member")//order Table에 있는 member 필드에 매핑된거야.
    //내가 매핑을 하는애가 아니고 나는 매핑된 거울일 뿐이야. (읽기 전용)
    private List<Order> orders = new ArrayList<>();
}
