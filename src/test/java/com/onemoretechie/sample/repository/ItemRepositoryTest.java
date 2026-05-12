package com.onemoretechie.sample.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.onemoretechie.sample.domain.Item;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @Test
    void savesAndReadsBack() {
        Item item = new Item("bolt", 12);
        item.onCreate(Instant.parse("2026-01-01T00:00:00Z"));

        Item saved = repository.save(item);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).hasValueSatisfying(i -> {
            assertThat(i.getName()).isEqualTo("bolt");
            assertThat(i.getQuantity()).isEqualTo(12);
            assertThat(i.getVersion()).isZero();
        });
    }
}
