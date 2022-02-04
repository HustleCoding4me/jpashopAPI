# 배운점과 유의사항
<details>

<summary> <h1>어노테이션 </h1> </summary>

#### @PersistenceContext

이 어노테이션이 있으면 EntityManager가 주입된다.
      build.gradle의 jpa보고 yml 파일 읽어서 설정된 대로 만들어서 DI 해줌.
      EntityManger 객체는 JPA에서 CRUD를 호출하는 기능
     
```java
   @Repository
public class MemberRepository {

    /*이 어노테이션이 있으면 EntityManager가 주입된다.
      jpa보고 yml 파일 읽어서
     */
    @PersistenceContext
    private EntityManager em;
```
#### @Embeddable (내장타입 대상 클래스의 상단) @Embedded (내장타입을 사용하는 객체의 변수 설정 위)

Jpa의 내장타입이란 뜻입니다. Jpa에서 domain 생성시 경우에 따라 안에 들어가는 POJO객체

```java
@Embeddable//jpa의 내장타입이란 뜻
@Getter @Setter
public class Address {

    private String city;
    private String street;
    private String zipcode;

}




@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded//둘 중 하나만 있어도 됨
    private Address address;

    private List<Order> orders = new ArrayList<>();//
}


```

#### @Inheritance(strategy = InheritanceType.SINGLE_TABLE) (strategy = InheritanceType.JOINED) (strategy = InheritanceType.TABLE_PER_CLASS)
```java
/*
* /strategy  join이 제일 정교, single 다 때려박기, table_per_class 상속받는 테이블마다 전부 생성
*/
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)/
@Getter @Setter
public abstract class Item {//상속관계 전략을 심어줘야한다. (여긴 Single Table)

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;
}
```

#### @OneToMany(mappedBy = "FK가 있는 다수에서 선언된 field명") 
       @OneToMany(mappedBy = "car")
(차(1) ArrayList<Tire> list - 타이어(*) Car car, Member(1) - Order(*) 1 대 다에서 종속을 의미")

```java
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
```

#### @DiscriminatorColumn(name = "dtype") -> @DiscriminatorValue("M")
      구현체와 상속받아 사용되는 Entity간의 구별
```java
      
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)//strategy =  제일 정교 single 다 때려박기
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {//상속관계 전략을 심어줘야한다. (여긴 Single Table)

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;
}

      
      
      
@Entity
@Getter @Setter
@DiscriminatorValue("M")
public class Movie extends Item {

    private String director;
    private String actor;
}

```
      
#### @Enumerated(EnumType.STRING) (EnumType.ORDINAL) 
```java
 //Enum Type에서 String 상태값으로 바로 들어가는 것. / 숫자로 연계되서 상태값으로 들어가는것 (1,2 이렇게) 가급적 사용 X(중간에 상태 하나 추가되면망함)
      
      @Entity
@Getter @Setter
public class Delevery {
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; //READY, COMP
}


```
      
#### @OneToOne
      
     1대1 매핑이라 더 자주 쓰이는 Table에 FK를 놓는다. (어디다 놔도 가능한데 가급적)
     연관관계 주인을 FK에 가까이에 있는 자주쓰이는 Table을 주인으로 둔다.
      
```java
      
@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @OneToOne
    @JoinColumn(name = "delivery_id")
    private Delevery delevery;
}//FK를 가지고 있는 주인. JoinColumn으로 delivery_id 연결해놓는다.
      
@Entity
@Getter @Setter
public class Delevery {

    @OneToOne(mappedBy = "delevery")
    private Order order;

}//종속관계가 되어버린 Delivery는 mappedBy로 Order의 수정에 의해서만 수정되는 거울로 만들어놓는다.

      
```
      
#### @Transactional()
>JPA의 Data 변경 모든 동작들은 @Transactional()안에서 이루어져야 LAZY 로딩 등의 동작들이 작동한다.
>주로 Spring  로직이 많이 사용되었으니 javax의 Transactional보단 spring의 Transactional이 더 사용할 수 있는 도구 개수가 많다.
```java
   @Transactional
public class MemberService {

```
      
> 읽기(select)에는 가급적이면 @Transaction(readOnly = true)를 넣어주면 좋다.      
> 영속성 컨텍스트를 flush안하고 dirtyCheck을 안하고 db에 따라서는 읽기 전용 Transaction에 대한 이점이 있어서 리소스가 덜 사용 되기도 한다.
> 메서드에 설정해줌      
```java
    @Transactional(readOnly = true)
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

```   
#### @XtoOne  fetchType EAGER to LAZY
> 필수! X to One 어노테이션들은 fetchType이 EAGER (자신 객체 불러올때 (XtoOne)으로 연관지어진 객체들 즉시 모두 가져오는것) 으로 설정되어있다.
> 이론상 가져오는 상대방은 1개의 객체라 즉시 불러오는게 합리적인것 처럼 보이나, 실상 JPQL로 불러올 때, select문으로 가져오기 때문에, 100개의 Many 진영의 객체를 가져온다면
> 각 1개를 가져올때마다 단문의 쿼리를 100번씩 수행할 수도 있다. (one쪽의 전체 select) 필수적으로 X to One으로 매칭된 애들은 fetchType을 LAZY로 수정해줘야한다.
   
> EX)
```java
      
     @OneToOne(mappedBy = "delevery", fetch = FetchType.LAZY)
    private Order order; 
      
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") //One의 참조키의 원래 column명
    private Order order;  
      
```
    
   
> X to Many에서 원래는 persist(orderItemA),persist(orderItemB),persist(orderItemC), persist(order)
> orderItems 위에 cascade = CascadeType.ALL 타입을 붙이면 persist(order)만 해도 orderItem들이 종속적으로 persist가 된다.
  
```java
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();
```
#### Autowired 대체 생성자 만들기
> //field에 final 변수가 있는 애들의 생성자를 만들어준다.   
```java
//1.기본 Autowired
@Service
@Transactional
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;
      
      
//2.Setter 만들고 그 위에 @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    public void setMemberRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
//한번 MemberService가 생성되고 컴파일 시점에 setter가 실행되어서 레포지토리가 변경될 수 있기때문에 비추
      
      
      
//3.Constructor
public class MemberService {


private MemberRepository memberRepository;

public MemberService(MemberRepository memberRepository) {
  this.memberRepository = memberRepository;
}
//클래식한 좋은 방법이다. Spring 버전이 업그레이드되면서 생성자가 한개라면, 굳이 @Autowired 어노테이션을 붙이지 않아도 자동으로 주입해준다.
      

//4.Lombok 사용
      
      //4-1.@AllArgsConstructor
      @AllArgsConstructor
      public class MemberService {
         private MemberRepository memberRepository;
      }
      //기본 생성자를 대신 만들어준다.
      
      //4-2.@RequiredArgsConstructor
      @RequiredArgsConstructor
      public class MemberService {
          private final MemberRepository memberRepository;
      }
      //제일 best, final이 달린 아이들의 Constructor만 만들어준다.
```
> 변수에 final을 붙이면, Test시에 직접 객체를 주입해주지 않으면 오류가 나서 Test하기도 쉽다. 관리가 더 용이해짐.  
      
#### @NoArgsConstructor(access = AccessLevel.PROTECTED)
> 객체의 연관관계에 맞춰서 create 메서드를 작성해놨을 때, 남들이 기본 Constructor로 new 할 경우 생성 관리가 힘들어진다.
> new로 객체 생성을 막아주기 위해 기본 생성자를 protected로 표시 (new로 생성하지 말고 작성해둔 메서드로 하라고 암묵적인 합의)
```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
          //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDateTime(LocalDateTime.now());
        return order;
    }

}
      
// Order order = new Order(); 
```

#### Controller와 Form 주고받기에 사용되는 @
> @PathVariable, @ModelAttribute
> ex(item 리스트 수정 btn -> item 수정 Form -> 수정 완료 -> 다시 itemList)
```html
 <tr th:each="item : ${items}">
                <td th:text="${item.id}"></td>
                <td th:text="${item.name}"></td>
                <td th:text="${item.price}"></td>
                <td th:text="${item.stockQuantity}"></td>
                <td>
                    <a href="#" th:href="@{/items/{id}/edit (id=${item.id})}" 
                       <!-- th:href로 "@{/items/{id}/edit(id=${item.id})}" item의 id 값으로 get 요청 -->
                       class="btn btn-primary" role="button">수정</a>
                </td>
            </tr>      
```
> 수정버튼 클릭시 localhost:3001/items/300/edit 요청
```java
   /**
     * 상품 수정
     */
      //@PathVariable 이 달려있는 인자의 이름으로 URI 를 확인하여 값을 할당하고 있다.
      //localhost:3001/items/300/edit 요청 시, updateItemForm의 itemId로 300 할당
    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm bookForm = new BookForm();
        bookForm.setId(item.getId());
        bookForm.setName(item.getName());
        bookForm.setPrice(item.getPrice());
        bookForm.setStockQuantity(item.getStockQuantity());
        bookForm.setAuthor(item.getAuthor());
        bookForm.setIsbn(item.getIsbn());
        model.addAttribute("form", bookForm);
        return "items/updateItemForm";
    }      
``` 
> db에서 id에 해당하는 ITEM 가져와서 updateItemForm에 뿌려줌 (여전히 localhost:3001/items/300/edit)   

```html
<form th:object="${form}" method="post">
        <!-- id -->
        <input type="hidden" th:field="*{id}" />   
<button type="submit" class="btn btn-primary">Submit</button>
    </form>      
```      
> 그냥 해당 localhost:3001/items/300/edit 에 post 방식으로 재요청
```java
     /**
     * 상품 수정 완료
     */
    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@ModelAttribute("form") BookForm form) {
        Book book = Book.createBook(form.getName(), form.getPrice(), form.getStockQuantity(), form.getAuthor(), form.getIsbn());
        book.setId(form.getId());
        itemService.saveItem(book);
        return "redirect:/items";
    }      
``` 
> itemService에 saveItem에 기존 id가 있는지 없는지 check해서 없으면 merge, item리스트로 red   
> @ModelAttribute("form") BookForm form 은, BookForm class의 변수들을localhost:3001/items/300/edit 에 post 방식으로 넘어온 request 변수들과
> 자동 Binding 해줘서 채운 뒤, Controller에서 사용하게 하고, 이후 추가적으로 return 될 View 단에서도 "form.Id" 식의 이름으로 사용가능하게 한다.
</details>      

<details>

<summary> <h1>Test코드 </h1> </summary>

#### @RunWith(SpringRunner.class) - 스프링과 관련된 것으로 테스트할거란 표시
#### @Transactional - 테스트코드 위에 씌이면 자동 Rollback (없으면 에러)
(drop table -> yml에서
                  jpa:
                    hibernate:
                      ddl-auto: create  설정으로 매번 새로 create -> insert) 
#### @Rollback(value = false) 하면 Rollback 설정 제거

```java
@RunWith(SpringRunner.class)//스프링과 관련된 것으로 테스트할거야
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional//testCase에 있으면 rollback을 시킵니다.
    @Rollback(value = false)
    public void testMember() throws Exception {
     //testCode
    }
```

#### Test에서 insert 등 쿼리가 날아가지 않는 경우
> 방법 1. EntityManager를 Test 자바에서 @Autowired로 받아, flush 해준다.
> 방법 2. @Transaction이 자동으로 Rollback해주는걸 해당 메서드 위에 @Rollback(value = false)를 올려 롤백 취소를 해본다.
```java
   @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long savedId = memberService.join(member);

        //then
        entityManager.flush();
        Assert.assertEquals(member, memberService.findOne(savedId));
        /*
        //@Transactional 같은 트랜잭션에서 같은 Entity , ID값이 같으면 같은 영속성 Context에서 똑같이
        //관리가 되기 때문에 가능한 Test이다.
        실행시키면 select문만 나오는데,
        persist를 한다고 해서 DB에 Insert를 바로 하는 것이 아니다.
        database 트렌잭션이 commit될 때, 한번에 query 실행한다.
        @Transactional은 자동 rollback이므로, JPA 판단상 쿼리를 flush하지 않아
        insert문 안나간다. (영속성 context에서만 관리됨)
         */

    }
         */
      
```

#### 예외 발생 Test하기 (ex 중복 회원 가입)
< 예상되는 Exception class를 @Test의 expected옵션으로 넣어준다. (try,catch 코드 대신 사용가능)
< 그냥 오류없이 지나가버리고 성공이라 띄우는 것을 방지하기 위해 Assert의 fail 메서드를 사용해준다. 
< (해당 지점까지 오면 안된다는 것을 의미함. 오면 Test실패)                                                             
```java
      @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member mem1 = new Member();
        mem1.setName("kim");
        Member mem2 = new Member();
        mem2.setName("kim");
        //when
        memberService.join(mem1);
        memberService.join(mem2);
        /*try {
            memberService.join(mem2);
        }catch (IllegalStateException e){
            return;
        }*/
        //then
        Assert.fail("여기까지 오면 실패임, 위에서 예외가 터져서 빠져야한다.");
        /*
         *Assert
         */
    }
```

     
#### memory DB로 Test하기
> test에 resources dir를 만들고 application.yml로 db나 포트 등 설정을 따로 생성해준다.
> test에 있는 파일들은 최우선적으로 test 하위에 있는 application.yml을 먼저 찾는다.
![image](https://user-images.githubusercontent.com/37995817/151693328-60ce3bd0-1f7e-40af-a032-d5f4f92e0398.png)
### logging

-기본적으로 hibernate의 type을 trace로 설정해준다.

```yml
logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace //trace 추가해줌

```
-p6spy(외부 라이브러리)
 작동 순서
 
      1.DataSource를 래핑하여 프록시를 만듭니다.
      
      2.쿼리가 발생하여 JDBC가 ResultSet 을 반환하면 이를 만들어둔 프록시가 가로챕니다.
      
      3.내부적으로 ResultSet의 정보를 분석하고 p6spy의 옵션을 적용합니다.
      
      4.Slf4j 를 사용해 로깅합니다.
      
출처 : https://backtony.github.io/spring/2021-08-13-spring-log-1/

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
      
#### //==연관관계 메서드==//

> 양방향 관계에 있는 Entity끼리 자바에서도 활용하기 위해서 set 할 때 ,원자적으로 기능을 묶어서 더 편리하게 사용하는 것 (실수 방지차원도 있음)
```java
    
    @JoinColumn(name = "member_id")//포린키
    private Member member;
      
    public void setMember(Member member){
       this.member = member;
       member.getOrders().add(this);
    }

    /*
    public static void main(String[] args){
    Member member = new Member();
    Order order = new Order();
    
    order.setMember(member);
    //member.getOrders().add(order);의 코드를 실수로 빼먹을 수 있기 때문에 원자적으로 묶는 것
    }
     */
  
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) //order Item의 order와 mappedBy
    private List<OrderItem> orderItems = new ArrayList<>();

      
      public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
```

#### 도메인 모델 패턴 vs 트랜잭션 스크립트 패턴

> 도메인 모델 패턴 : 대부분의 비즈니스 로직이 엔티티에 있어서, 서비스계층은 단순히 에티티에 필요한 요청을 위임하는 역할만 하는 것

```java
      
//오더 서비스에서 주문 취소 로직이다. 단순히 order Entity에 이미 구현된 cancel을 호출하는 일   @Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {   
//==비즈니스 로직==//
/**
* 주문 취소
*/
public void cancel(){
  if (delivery.getStatus() == DeliveryStatus.COMP) {
      throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
  }

  this.setStatus(OrderStatus.CANCEL);
  for (OrderItem orderItem : orderItems) {
      orderItem.cancel();
  }
} 
}
      
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
      
          @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancel();
        //JPA가 변경 내역을 감지해서 database에 update쿼리를 날려준다.
        //order status, orderItem들의 stock update
    }
}
```

> 트랜잭션 스크립트 패턴 : 엔티티에는 비즈니스 로직이 거의 없고, 서비스 계층에서 대부분의 비즈> > 니스 로직을 처리하는 것 (기존 패턴)


#### 예외처리

```java
      
      package jpabook.jpashop.exception;
public class NotEnoughStockException extends RuntimeException {
 public NotEnoughStockException() {
 }
 public NotEnoughStockException(String message) {
 super(message);
 }
 public NotEnoughStockException(String message, Throwable cause) {
 super(message, cause);
 }
 public NotEnoughStockException(Throwable cause) {
 super(cause);
 }
}
```
      
      
#### //== JPA에서 기존 엔티티의 값을 수정하는 방법(부제 : 왜 em.merge() 사용을 지양해야하는가)

> 준영속Entity의 경우 (준영속Entity는 em.find를 한 것이 아닌, DB에는 존재하지만 순간적으로 자바에서 객체로만 관리되어져 EntitnyManager 1차캐시에 등록X 객체   > Update할 경우 2가지 방법이 존재한다.
> 1. 변경 감지를 수동으로 사용하는 법 (영속 Entitny로 등록해줘서 dirtyChecking으로 수정되게 하는 법)
> 2. 직접 merge()를 사용하는 법이다.
> 결과적으로 말하자면 merge 사용은 지양해야한다.      
> 변경 감지는 따로 updateMethod를 파서 해당 id로 em.find를 시켜 영속성을 만들어 1차캐시에 저장하고, 이후 그 객체를 변경해주면 이후 tx 커밋시에 자동 등록된다.
> merge도 이와 비슷한 맥락인데, 1차 캐시에서 찾다가 없으면 db에서 꺼내온다. 여기서 영속성이 생기고, 이후 찾아온 객체에 merge(parameter)로 받은 파라미터에서
> 모조리 set 해주어 변경감지가 되게 만들어준다.
> 그런데 문제는 param에 넘기는 객체에 특정 변수의 값이 없을 경우, null로 update한다. 따라서 merge에 보내는 변경을 원하는 param 객체에는 
> 변경을 원하는 변수를 제외한 모든 기존 변수의 값이 setting 되어있는 상태여야 한다.
> 위험하니까 그냥 스스로 변경감지를 만들도록 하자. 

> 결론 : Update한답시고 어설프게 Controller에서 객체 new 하지말고, service에서 find해서 merge 대신 변경감지로 update치자.
> 트랜잭션이 있는 서비스 계층에 식별자 (`id`)와 변경할 데이터를 명확하게 전달하자(파라미터 or dto)    
> * 수동 set set 으로 하는게 좋다.      
      
```java
 @Test
    public void updateTest() throws Exception {
        Book book = em.find(Book.class, 1L);
        book.setName("asdfasd");

        //변경감지 == dirty checking
        //em.find시에 해당 book을 영속성 관리하고,
        //book의 Name 변경을 감지해서 추후 flush()시에 변경해준다.

        /**
         * but, 준영속 엔티티인경우 문제가 된다.
         * 실재로 DB에 갔다 와서, 영속성 컨텍스트가 더는 관리하지 않는 엔티티
         *  DB에 한번 저장되어 식별자가 존재한다. 임의로 만들어낸 엔티티도 기존 식별자를 가지고
         * 있으면 준영속 엔티티로 볼 수 있다.
         *  ex Book book = new Book();
         *  book.setId(form.getId(); 등으로 생성되면, DB엔 있지만 EntityManager가 모르는 아이
         */

        /**
         * 이런 준영속 Entiy를 수정하는 2가지 방법
         * 1.변경 감지를 사용하는 법
         * @Transactional
         *     public void updateItem(Long itemId, Book param) {
         *         Item findItem = itemRepository.findOne(itemId);
         *         findItem.setPrice(param.getPrice());
         *         findItem.setName(param.getName());
         *         findItem.setStockQuantity(param.getStockQuantity());
         *
         *     }
         * 2. merge를 사용하는것(비추)
         * Book book = new Book();
         * book.setId(form.getId();
         * em.merge(item);
         *
         * merge 동작 순서
         * 1.merge(파라미터) 파라미터로 넘어온 준영속엔티티 식별자 값으로 1차 캐시에서 Entity를 조회한다
         * 2.만약 1차 캐시에서 엔티티가 없으면 db에서 조회, 1차캐시에 저장
         * 3. 조회한 영속 엔티티에 파라미터로 받은 값을 채워 넣는다. (여기서 변경된 값 set)
         * 4. 영속 상태인 채워진 엔티티를 return 한다.
         * 5. 결과적으로 영속상태의 수정된 Entity가 나중에 flush될 때 dirtyChecking이 된다.
         *
         * * why? 사용하지 말라고 하는 것인가?
         *         위에 변경감지법과 똑같은 코드다.
         *         결국 id로 찾아서 parameter로 merge에 넘긴 값으로
         *         찾아온 것의 값을 다 바꿔치기한 뒤, 변경 감지 시키는 법이다.
         *
         *  기존 merge의 Param으로 넣은 것과, merge()가 return한 객체는 다른 객체다.
         *  주의 !// 병합을 사용하면 모든 속성을 교체한다.
         *  병합시 param으로 넘긴 값으로 모두 갈아치워서 update치기 때문에,
         *  param으로 넘긴 객체에 값이 없으면 tx commit시에 null로도 교체가 된다.
         *  안됨 안됨!!
         *
         * 결론 : Update한답시고 어설프게 Controller에서 객체 new 하지말고, service에서 find해서 merge 대신 변경감지로 update치자.
         * 수동 set set 으로 하는게 좋다.
        * /
    }      
      
```      
      
   
      
      
</details>
      
<details>

<summary> <h1>Form에서 Validation Check </h1> </summary>

#### Controller 단에서 처리
> @Valid는 MemberForm객체에 선언된 각종 제약조건 ex)@NotNull 등을 체크해준다.
> 이후 후미에 BindingResult 객체를 받으면 그 객체로 결과들을 담아주는데,
> 오류가 있다면, 결과를 담아 다시 Form으로 넘겨 thymeleaf로 처리해서 Form에서 오류 자체 메세지를 출력할 수 있다.
```java
@Controller
@RequiredArgsConstructor
public class MemberController {
      
@PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {

        if (result.hasErrors()) {
            return "members/createMemberForm";
        }
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }    

}
      
```
```java
@Getter @Setter
public class MemberForm {

    @NotEmpty(message = "회원 이름은 필수 입니다.") //@Valid가 처리해줄 조건 어노테이션
    private String name;

    private String city;
    private String street;
    private String zipcode;
}
      
```
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<style>
 .fieldError {
 border-color: #bd2130;
 }
</style>
<body>
<div class="container">
  <div th:replace="fragments/bodyHeader :: bodyHeader"/>
  <form role="form" action="/members/new" th:object="${memberForm}"
        method="post">
    <div class="form-group">
      <label th:for="name">이름</label>
      <input type="text" th:field="*{name}" class="form-control"
             placeholder="이름을 입력하세요"
             th:class="${#fields.hasErrors('name')}? 'form-control <!-- 여기서 BindingResult를 가져와서 fields.hasErrors 식으로 처리 가능하다. -->
fieldError' : 'form-control'">                                     <!-- 오류나면 border를 빨갛게 두르게 css style 적용해서 class 선언해줌 -->
      <p th:if="${#fields.hasErrors('name')}"                      <!-- th:if 만약 name문의 @NotNull 조건이 @Valid에서 오류로 잡히면, BindingResult를 fields로 불러 오류가 있다고 -->
         th:errors="*{name}">Incorrect date</p>                    <!-- 판단이 될 것이고, th:errors="*{name}"이 MemberForm에 미리 선언해둔 message를 담아 출력해준다. --> 
    </div>                                                         <!-- @NotEmpty(message = "회원 이름은 필수 입니다.") -->
</body>
</html>      
```
      
![image](https://user-images.githubusercontent.com/37995817/152294021-0d062093-f49d-4e01-85d5-2331640d915a.png)

> 장점 : MemberForm 객체에 이미 도시나 적혀있는 우편번호들이 저장이 유지가 되어있기 때문에,   
> 다시 Controller를 갔다 와서 name을 Validation했다고 해서 기존 유저가 적어놓은 내용은 그대로 > 유지가 된다.
</details>
