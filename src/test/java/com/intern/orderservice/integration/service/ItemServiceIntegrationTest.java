package com.intern.orderservice.integration.service;

import com.intern.orderservice.dto.request.CreateItemRequest;
import com.intern.orderservice.dto.response.ItemResponse;
import com.intern.orderservice.exception.ItemAlreadyExistsException;
import com.intern.orderservice.integration.CustomPostgreSQLContainer;
import com.intern.orderservice.integration.NoSecurityConfig;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.repository.ItemRepository;
import com.intern.orderservice.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import({NoSecurityConfig.class})
@ActiveProfiles("test")
@Tag("integration")
@Transactional
class ItemServiceIntegrationTest extends CustomPostgreSQLContainer {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testCreateItemSuccess() {
        CreateItemRequest request = new CreateItemRequest("Laptop", new BigDecimal("1200.00"));

        ItemResponse response = itemService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.price()).isEqualByComparingTo("1200.00");

        // Verify persisted
        Optional<Item> saved = itemRepository.findById(response.id());
        assertThat(saved).isPresent();
    }

    @Test
    void testCreateItemDuplicateThrowsException() {
        // Arrange: save item first
        itemRepository.save(new Item(null, "Phone", new BigDecimal("800.00"), null));

        CreateItemRequest duplicate = new CreateItemRequest("Phone", new BigDecimal("800.00"));

        // Act + Assert
        assertThatThrownBy(() -> itemService.create(duplicate))
                .isInstanceOf(ItemAlreadyExistsException.class);
    }

    @Test
    void testGetByIdReturnsItem() {
        Item saved = itemRepository.save(new Item(null, "Tablet", new BigDecimal("500.00"), null));

        Optional<ItemResponse> response = itemService.getById(saved.getId());

        assertThat(response).isPresent();
        assertThat(response.get().name()).isEqualTo("Tablet");
    }

    @Test
    void testGetByIdNotFoundReturnsEmpty() {
        Optional<ItemResponse> response = itemService.getById(999L);
        assertThat(response).isEmpty();
    }

    @Test
    void testGetAllReturnsPagedItems() {
        itemRepository.save(new Item(null, "Keyboard", new BigDecimal("100.00"), null));
        itemRepository.save(new Item(null, "Mouse", new BigDecimal("50.00"), null));

        Page<ItemResponse> page = itemService.getAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(ItemResponse::name)
                .containsExactlyInAnyOrder("Keyboard", "Mouse");
    }

    @Test
    void testDeleteItemSuccess() {
        Item saved = itemRepository.save(new Item(null, "Monitor", new BigDecimal("300.00"), null));

        itemService.delete(saved.getId());

        assertThat(itemRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void testDeleteItemNotFoundThrowsException() {
        assertThatThrownBy(() -> itemService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
