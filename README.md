# 배운점과 유의사항
<details>

<summary> <h1>어노테이션 </h1> </summary>
      
#### @RestController (//@Responsebody + @Controller 합친것)
```java
@RestController 
@RequiredArgsConstructor
public class MemberApiController {
}
      
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
public @interface RestController {
}      
```

#### @ResponseBody //json으로 온 Body를 Member에 그대로 Mapping

```java
 @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        //json으로 온 Body를 Member에 그대로 Mapping
    }      
```      
      
</details>      

<details>

<summary> <h1>Test코드 </h1> </summary>



</details>

<details>

<summary> <h1>yml 파일 </h1> </summary>      



```yml
spring:
  datasource: //db설정
    url: jdbc:h2:tcp://localhost/~/jpashop
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create  //db 자동으로 drop, create (매번)
    properties:
      hibernate:
        format_sql: true  //sql을 표기

logging.level:
  org.hibernate.SQL: debug //debug모드로 sql을 log 찍어준다.

server:
  port: 9091

```

</details>      
      
<details>
<summary> <h1>유의사항 </h1> </summary>
      
#### //== API 사용을 할 때 DTO를 만들어서 받는 이유==//
      
> API 스펙에 맞춰서 @ResponseBody Entitny를 사용하는게 아니라 DTO를 하나 만들어서 해야한다.
> Entity를 사용하게되면 어디까지 API에서 받고 Binding 되는지, 추가적으로 다른 코드에서 Binding 했는지 모를 수도 있다. (모든 변수가 들어올 가능성이 있으므로)
> 따라서 DTO에 해당 API를 FIT하게 맞춰서 딱 받는 스펙을 알 수 있다. 외부에 Entity를 보여줘서도 안됨.

> 받은 Request를 CreateMemberRequest DTO를 바인딩하여 생성, CreateMemberResponse로 전송      
```java
       @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        member.setAddress(request.getAddress());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberRequest {
        private String name;
        private Address address;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
      
```
      
#### //== API 제작시 DTO 생성시에 혹여 DTO 내부에 Entity가 있는지 check 해야한다.==//
 
> Entity가 변하면 모두 엉망이 되어버림.
      
```java
@Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //OrderItem도 Entity기 때문에 바로 반환하면 안된다. OrderItem도 DTO로 모두 변환해야함!!!
        private List<OrderItem> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDateTime();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
            this.orderItems = order.getOrderItems();
        }
    }   
      
```      
      
> Order 하위에 OrderItem Entitny를 Dto로 수정하는 경우

```java
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
      
```      
> OrderItemDto에 선언하여 Return하고 싶은 값을 추릴 수 있다.      
 ![image](https://user-images.githubusercontent.com/37995817/153432016-40f8316a-4839-46d6-912c-604540ffc34c.png)
     

#### //==fetch join시에 XtoMany에서 List Collections들과 join시에 중복 데이터 삭제하는법 ==//

> ex) Order를 불러올 때, orderItems도 join해서 부르면 orderItems의 개수만큼 중복 Order가 불려온다.
      
> Order에 연결된 orderItems 개수만큼 중복된 모습      
 ![image](https://user-images.githubusercontent.com/37995817/153604400-2380f33c-e334-4930-a9c6-e3671e7fea57.png)

> fetch join 나쁜 예     
```java
public List<Order> findAllWithItem(OrderSearch orderSearch) {
        return em.createQuery("select o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" +
                " join fetch oi.item i", Order.class).getResultList();
    }      
```      
> 여기서 JPA의 distinct를 적용하면, 동일한 id인 order 객체는 제거하여 준다. (4개 찾아올거 2개 찾아옴)

> JPA에 distinct를 적용하여 order id당 한개씩만 가져온 모습
![image](https://user-images.githubusercontent.com/37995817/153604556-2ca00bdd-386b-42aa-a652-b01044fb78d7.png)
      
> 기존 JPQL에 distinct만 추가해주었다. db의 distinct와 다른 점은 db값이 모두 동일하지 않아도, Order 객체의 id값이 동알히면 배제한다.
```java
    public List<Order> findAllWithItemDistinct(OrderSearch orderSearch) {
        return em.createQuery("select distinct o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" +
                " join fetch oi.item i", Order.class).getResultList();
    }      
```   
      
      
</details>
      
<details>

