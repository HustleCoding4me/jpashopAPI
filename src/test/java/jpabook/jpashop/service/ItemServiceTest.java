package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.controller.exception.NotEnoughStockException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Test
    public void 물건저장하기() throws Exception {
        //given
        Book b = new Book();
        b.setName("개미");
        //when
        Long savedId = itemService.saveItem(b);
        //then
        assertEquals(b,itemService.findOne(savedId));
    }

    @Test
    public void 물건찾기() throws Exception {
        //given
        Book b = new Book();
        b.setName("개미");
        Long savedId = itemService.saveItem(b);
        //when

        //then
        assertEquals(b,itemService.findOne(savedId));

    }

    @Test
    public void 재고늘리기() throws Exception {
        //given
        Book b = new Book();
        b.setName("베르베르");
        Long savedId = itemService.saveItem(b);
        Book savedBook = (Book) itemService.findOne(savedId);
        System.out.println(" === Origin Stock Quantity : " + itemService.findOne(savedId).getStockQuantity()+"개 ===");

        //when
        savedBook.addStock(10);
        savedBook = (Book) itemService.findOne(savedId);
        savedBook.addStock(10);
        savedBook = (Book) itemService.findOne(savedId);
        //then
        assertEquals(20,savedBook.getStockQuantity());
    }

    @Test(expected = NotEnoughStockException.class)
    public void 재고줄이기_오류검증() throws Exception {
        //given
        Book b = new Book();
        b.setName("베르베르");
        Long savedId = itemService.saveItem(b);
        Book savedBook = (Book) itemService.findOne(savedId);
        savedBook.addStock(10);
        System.out.println(" === Origin Stock Quantity : " + itemService.findOne(savedId).getStockQuantity()+"개 ===");

        //when
        savedBook = (Book) itemService.findOne(savedId);
        savedBook.removeStock(11);
        //then
        fail("재고가 부족한데 줄이려고 하는게 성공됨");
    }
    @Test
    public void findItems() {
    }

    @Test
    public void findOne() {
    }
}