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

    private List<Order> orders = new ArrayList<>();
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




