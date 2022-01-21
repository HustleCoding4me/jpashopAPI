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

