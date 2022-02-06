package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne (List 객체가 아닌 관계)
 * Order
 * Order -> Member  //ManyToOne
 * Order -> Delievery // OneToOne
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /*
    무한 루프를 돈다. 두 Entitny 중 하나는 @JsonIgnore 해줘야 한다.
    또한 Order 의 Member를 가져올 때, fetch가 LAZY기 때문에 프록시객체를 가져와서
    가져올 수 없다. 프록시 에러가 난다.
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        return all;
    }
}
