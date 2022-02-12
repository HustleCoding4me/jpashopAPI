package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * X to Many (List등 컬렉션) 다루기
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;


    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        //한번씩 호출하면서 LAZY LOADING 호출
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2(){
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    //fetch join법 근데 쿼리에 distinct 적용함
    //db distinct는 한 줄이 아예 같아야 제거하는데 JPA distinct는 Entity를 가져올 때 같은 id값이면 버린다.
    //Order의 객체를 보니 id값이 똑같다 = 지움
    @GetMapping("/api/v3/ordersNotDistinct")
    public List<OrderDto> orderV3(){
        List<Order> all = orderRepository.findAllWithItem(new OrderSearch());
        return all.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());
    }

    @GetMapping("/api/v3/ordersDistinct")
    public List<OrderDto> orderV3Dt(){
        List<Order> all = orderRepository.findAllWithItemDistinct(new OrderSearch());
        return all.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());
    }

    @GetMapping("/api/v3.1/ordersCollectionPaging")
    public List<OrderDto> orderV3_CollectionPaging(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value="limit", defaultValue = "100") int limit)
            {
        List<Order> orders = orderRepository.findAllWithMemberDeliveryWithPaging(offset,limit);

        return orders.stream().map(o -> new OrderDto(o)).collect(Collectors.toList());
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }
    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //OrderItem도 Entity기 때문에 바로 반환하면 안된다. OrderItem도 DTO로 모두 변환해야함!!!
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDateTime();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            this.orderItems = order.getOrderItems().stream()
                    .map(o -> new OrderItemDto(o))
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
