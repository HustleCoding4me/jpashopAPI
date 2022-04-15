# 배운점과 유의사항
<details>  
.
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
<summary> <h1>OSIV  </h1> </summary>      
      
#### OSIV
> Open Session in view : 하이버네이트
> Open EntityManager in view : JPA 
> 관례상 OSIV라고 한다. (JPA 가 나중에 나옴)

> 서버시작때 warn을 주는 모습 
![image](https://user-images.githubusercontent.com/37995817/153759811-4d33c615-694c-43e6-9ca4-2f17bd6bcbee.png)
     
```java
    2022-02-14 00:12:00.577  WARN 19764 --- [  restartedMain] JpaBaseConfiguration$JpaWebConfiguration : spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning   
```   
      
> OSIV 기본값은 true인데, true로 되어있으면 영속성 컨텍스트가 Transaction (Service -> Repository) 끝나도, Controller, View, Response 끝날 때 까지
> 영속성 Context란 DB Connection이 끝까지 살아있다. (View Render, Data Response Return 되면 사라진다.)
> 그렇기 때문에 API, View Template에서 지연 로딩사용이 가능했다.
> 지연로딩은 영속성 Contenxt가 살아있어야 가능하고, 영속성 컨텍스트는 기본적인 데이터베이스 커넥션을 유지한다.
      
#### OSIV 치명적인 단점!
      
> 너무 오랜 시간동안 DB Connection 리소스를 사용하기 때문에, 실시간 트래픽이 중요한 어플리케이션에서는 Connection이 모자랄 수 있다. 이건 결국 장애로 이어짐.
> 보통은 Service -> Repository에서 DB 값 가져오고 끝나는데, OSVI는 true이면 계속 클라이언트에게 Response 갈 때까지 물고있기 때문에
> ex) 컨트롤러에서 외부 API를 호출하면, 외부 API 대기시간만큼 커넥션 리소스를 반환하지 못하고 대기해야한다.

> 영속성 컨텍스트 생존 범위, 수정 범위      
![image](https://user-images.githubusercontent.com/37995817/153760084-8c27741b-ee40-499b-b4fe-7733f6bebe9a.png)
      
      
#### OSIV false시에
      
![image](https://user-images.githubusercontent.com/37995817/153760123-4e11b788-76df-4214-b1f0-41ad786e072c.png)

> Service, Repository범위에서만 영속성 Context 유지, DB Connection 유지

> Service -> Repository에서 영속성 컨텍스트를 받고, DB Connection도 반환한다. :ㅣ Connection 리소스를 낭비하지 않는다.
      

#### OSIV false 단점
      
> OSIV를 끄면, 모든 지연로딩을 트랜잭션 안에서만 처리해아한다. 따라서 지금까지 작성한 많은 코드가 Controller에서 처리한 경우가 많은데, 모두 트랜잭션 안에 
> 밀어넣어야 한다. 그리고 viewTemplate에서 LAZY LOADING이 지원되지 않는다. 
     
      
#### OSIV true, false Test
![image](https://user-images.githubusercontent.com/37995817/153760321-7d7d6adf-bf57-4f90-996b-db5ffba01138.png)

> Controller 시점에서 LAZY LOADING 호출하는 v1 API      
```java
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
      
```   
> OSIV true 시에는 잘나오는데, false 시에는 Proxy 에러가 난다.      
      
![image](https://user-images.githubusercontent.com/37995817/153760380-ee68289b-87b4-44ed-9a01-c7c437d2a89f.png)

> OSIV false라서 Controller에는 Transaction이 이미 반환되어서 DB Connection, 영속성 Context가 반환되어있기 때문.

      
> Connection 부족 문제를 해결하기 위해 OSIV false로 뒀을 때, 해결방안 -> @Transaction을 붙인 Service를 새로 파서 거기서 LAZYLOADING 작업 수행 뒤, Controller 반환

      
> 기존 Controller에 있는 메서드, OSIV true 로써, Controller에서 LAZY LAODING을 이용해 OrderDto로 변환하는 모습이다.      
```java
   public List<OrderDto> orderV3(){
        List<Order> all = orderRepository.findAllWithItem(new OrderSearch());
        return all.stream().map(o -> new OrderDto(o)).collect(toList());
    }      
```      
      
      
```java
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public List<OrderDto> orderV3(){
        List<Order> all = orderRepository.findAllWithItem(new OrderSearch());
        return all.stream().map(o -> new OrderDto(o)).collect(toList());
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //OrderItem도 Entity기 때문에 바로 반환하면 안된다. OrderItem도 DTO로 모두 변환해야함!!!
        private List<OrderApiController.OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDateTime();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            this.orderItems = order.getOrderItems().stream()
                    .map(o -> new OrderApiController.OrderItemDto(o))
                    .collect(toList());
        }
    }
```   
     
> 필수 로직이 새로 생성된 @Transaction 범위 안의 Service로 빠진 Controller 메서드 모습.
```java
   @GetMapping("/api/v3/ordersNotDistinct")
    public List<OrderDto> orderV3(){
        return orderQueryService.orderV3();
    }      
```   
      
> 쿼리용 전용 Service를 만들고, Transaction을 처리하는 로직 삽입한다.

#### OSIV false시에 해야할 일

> ->OrderService : 핵심 비즈니스 로직
> ->OrderQueryService : 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)
이런 식으로 관심사를 명확하게 분리하는 선택을 택한다.
      
#### 결론
      
> 1. OSIV true시에 Connection은 부족하지만, OrderQueryService같은것을 생성하지 않아도 그냥 Controller에서 지연로딩같은 것들을 사용해도 된다.      

> 2. 코딩을 생각하면 키지만, 성능을 생각하면 끄는게 맞다.      
      
> 3. 고객 서비스를 많이 제공하는 실시간, API 서버등은 끄고 간다. 근데 admin System은 많이 안쓰기 때문에 그냥 키고 생성한다.   
      
      
</details>
      
      
      go
     

