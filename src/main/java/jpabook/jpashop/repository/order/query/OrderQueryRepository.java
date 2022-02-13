package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Entity가 아닌 화면이나 API용으로 DTO들을 가져올 때 (화면에 FIT하게)
 *
 */

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * Collection은 못가져온다.
     * @return
     */
    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); //query 1번 -> N개

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // Query N번
            o.setOrderItems(orderItems);
        });
        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class).setParameter("orderId", orderId).getResultList();
    }

    //V4 Dto로 Collection 처리
    public List<OrderQueryDto> findOrders() {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDateTime, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    //V5 기존 stream을 통해 OrderDto에 OrderItems LAZYLOADING 초기화
    //해주고 Items 초기화를 하기 위해 개별적인 orderitem & item select query를
    //넘어서 In절로 한번에 가져온다.

    /**
     * Order와 x To One인 아이들 최대한 fetch join, join 해서 먼저 가져온다.
     * LAZY LOADING인 Order.Collection들 (Order(1)-OrderItems(N))을
     * IN 구문을 통해 각 Order에 해당하는 OrderItems 개수만큼만 호출되던 쿼리 수를
     * DB에선 단 한번에 모든 Order와 연결되어있는 OrderItems를 한번에 호출하게 변함
     *
     * 이후 가져온 orderItems 리스트를 OrderId 순으로 GroupingBy 해서
     * key : order_id, value : list of OrderItems
     * Order에 해당 id와 맞는 OrderItems list를 꺼내 set 해준다.
     *
     * 총 쿼리 = Orders 불러올 떄 1번 + 전체 OrderItems 꺼내올 떄 1번 = 2번
    * @return
     */
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> orders = findOrders();

        List<Long> orderIds = toOrderIds(orders);

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        orders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return orders;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
        return orderItemMap;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> orders) {
        return orders.stream().map(o -> o.getOrderId())
                .collect(Collectors.toList());
    }

    //V6 쿼리 1번에 해결하는 법
    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDateTime, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class
        ).getResultList();
    }
}
