package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

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

    public List<OrderQueryDto> findOrders() {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDateTime, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }
}
