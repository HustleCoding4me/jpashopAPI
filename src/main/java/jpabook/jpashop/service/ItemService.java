package jpabook.jpashop.service;

import jpabook.jpashop.controller.BookForm;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = false)
    public Long saveItem(Item item) {
        return itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, BookForm param) {
        Item findItem = itemRepository.findOne(itemId);
        findItem.change(param.getName(), param.getPrice(), param.getStockQuantity());
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long item_id) {
        return itemRepository.findOne(item_id);
    }


}
