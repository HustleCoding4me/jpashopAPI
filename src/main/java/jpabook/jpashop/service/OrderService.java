package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        //엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);
        //주문 저장
        /**
         * Order에 들어있는 orderItem, delivery를 save해주지 않아도
         * Order에 들어있는 변수속성에 cascade = CascadeType.ALL로 매치되어있어
         * 자동으로 Order가 persist시에 같이 persist해준다.
         *
         * //==CASCADE 범위==//
         * Order의 경우, Delivery, Item을 관리해서 사용된거다.
         * delivery, item은 Order만 참조해서 사용되기 때문에
         * 라이프사이클이 동일하게 관리가 된다면, 다른것이
         * 참조할 수 없는 private OWner면 사용하면 좋다.
         */
        orderRepository.save(order);

        return order.getId();
    }
//취소

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancel();
        //JPA가 변경 내역을 감지해서 database에 update쿼리를 날려준다.
        //order status, orderItem들의 stock update
    }

    //검색
/*
    public List<Order> findOrders(OrderSearch orderSearch) {
        return OrderRepository.findAll(orderSearch);
    }*/
}
