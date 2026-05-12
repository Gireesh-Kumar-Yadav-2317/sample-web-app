package com.onemoretechie.sample.service;

import com.onemoretechie.sample.domain.Item;
import com.onemoretechie.sample.repository.ItemRepository;
import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository repository;
    private final Clock clock;

    public ItemService(ItemRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Item> findAll() {
        return repository.findAll();
    }

    public Item findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item " + id + " not found"));
    }

    @Transactional
    public Item create(String name, int quantity) {
        Item item = new Item(name, quantity);
        item.onCreate(clock.instant());
        return repository.save(item);
    }

    @Transactional
    public Item update(Long id, String name, int quantity) {
        Item item = findById(id);
        item.setName(name);
        item.setQuantity(quantity);
        item.onUpdate(clock.instant());
        return repository.save(item);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Item " + id + " not found");
        }
        repository.deleteById(id);
    }
}
