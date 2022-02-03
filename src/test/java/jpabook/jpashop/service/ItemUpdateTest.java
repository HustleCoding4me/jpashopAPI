package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ItemUpdateTest {

    @Autowired
    EntityManager em;

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
         *


         * 이런 준영속 Entiy를 수정하는 2가지 방법
         * 1.변경 감지를 사용하는 법
         *
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
         * 결론 : merge 대신 변경감지로 그냥 find로 영속성에 등록해서
         * 수동 set set 으로 하는게 좋다.
                **/
    }

}
