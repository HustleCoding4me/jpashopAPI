# 배운점과 유의사항

### 어노테이션

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
      

### Test코드

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


### yml 파일


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

### //==연관관계 메서드==//
#### 양방향 관계에 있는 Entity끼리 자바에서도 활용하기 위해서 set 할 때 ,원자적으로 기능을 묶어서 더 편리하게 사용하는 것 (실수 방지차원도 있음)
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

