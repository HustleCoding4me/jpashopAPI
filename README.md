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




