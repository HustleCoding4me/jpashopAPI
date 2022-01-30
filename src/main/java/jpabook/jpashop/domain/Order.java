package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")//포린키
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) //order Item의 order와 mappedBy
    private List<OrderItem> orderItems = new ArrayList<>();

    //원래는 persist(orderItemA),persist(orderItemB),persist(orderItemC), persist(order)
    //orderItems 위에 cascade = CascadeType.ALL 타입을 붙이면 persist(order)만 해도 orderItem들이 종속적으로 persist가 된다.

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delevery delevery;

    private LocalDateTime localDateTime;//자바 8부터 하이버네이트가 자동 지원

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL] ENUM


    //==연관관계 메서드==//
    public void setMember(Member member){
       this.member = member;
       member.getOrders().add(this);
    }

    /*
    public static void main(String[] args){
    Member member = new Member();
    Order order = new Order();

    order.setMember(member);
    //member.getOrders().add(order);의 코드를 실수할 수 있기때문에 원자적으로 묶는 것
    }
     */
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delevery delevery) {
        this.delevery = delevery;
        delevery.setOrder(this);
    }
}
