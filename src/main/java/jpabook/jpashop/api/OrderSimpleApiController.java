package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

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
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;
    /*
    무한 루프를 돈다. 두 Entitny 중 하나는 @JsonIgnore 해줘야 한다.
    또한 Order 의 Member를 가져올 때, fetch가 LAZY기 때문에 프록시객체를 가져와서
    가져올 수 없다. 프록시 에러가 난다.
     */
    //Entity 직접 노출은 좋지 않다.
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        return all;
    }
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        //ORDER 2개 가져옴
        // 1 + 회원 N  + 배송 N 총 1+ 2+ 2 5번 쿼리가 수행됨
        List<Order> all = orderRepository.findAll(new OrderSearch());
        //SimpleOrderDTO를 생성하면서 LAZYLOADING때문에 getName, getDelivery할 때 마다
        //가져옴. N+1 문제임
        return all.stream().map(SimpleOrderDto::new)
                .collect(toList());
    }

    //fetch Join 버전. 최초 가져올 때 관련 객체를 다 가져와서
    //1 + N 문제가 나지 않는다.
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> allWithMemberDelivery = orderRepository.findAllWithMemberDelivery();
        return allWithMemberDelivery.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    //직접 DTO를 쿼리에서 생성해서 받아온다. V3보다 select 개수가 좀 적은데
    //엄청난 성능차이를 보이지 않지만, 고성능이 요구되는 경우 고려해볼만 하다.
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }


    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDateTime();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
        }

    }
}
