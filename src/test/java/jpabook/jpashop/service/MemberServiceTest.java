package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager entityManager;


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

}