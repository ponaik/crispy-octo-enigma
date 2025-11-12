package com.intern.orderservice.unit.service;

import com.intern.orderservice.dto.request.CreateItemRequest;
import com.intern.orderservice.dto.response.ItemResponse;
import com.intern.orderservice.exception.ItemAlreadyExistsException;
import com.intern.orderservice.mapper.ItemMapper;
import com.intern.orderservice.model.Item;
import com.intern.orderservice.repository.ItemRepository;
import com.intern.orderservice.service.impl.ItemServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;

    @Mock
    ItemMapper itemMapper;

    @InjectMocks
    ItemServiceImpl itemService;

    @Captor
    ArgumentCaptor<Item> itemCaptor;

    private final Long ID = 1L;
    private final String NAME = "Widget";
    private final BigDecimal PRICE = new BigDecimal("9.99");

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns mapped ItemResponse when item exists")
        void returnsMappedItemResponseWhenItemExists() {
            // given
            Item item = new Item();
            item.setId(ID);
            item.setName(NAME);
            item.setPrice(PRICE);

            ItemResponse response = new ItemResponse(ID, NAME, PRICE);

            given(itemRepository.findById(ID)).willReturn(Optional.of(item));
            given(itemMapper.toItemResponse(item)).willReturn(response);

            // when
            Optional<ItemResponse> result = itemService.getById(ID);

            // then
            assertThat(result).isPresent();
            assertThat(result).contains(response);
        }

        @Test
        @DisplayName("returns empty when item does not exist")
        void returnsEmptyWhenItemDoesNotExist() {
            // given
            given(itemRepository.findById(ID)).willReturn(Optional.empty());

            // when
            Optional<ItemResponse> result = itemService.getById(ID);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("returns page of mapped ItemResponse")
        void returnsPageOfMappedItemResponse() {
            // given
            Item item = new Item();
            item.setId(ID);
            item.setName(NAME);
            item.setPrice(PRICE);

            ItemResponse response = new ItemResponse(ID, NAME, PRICE);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> page = new PageImpl<>(List.of(item), pageable, 1);

            given(itemRepository.findAll(pageable)).willReturn(page);
            given(itemMapper.toItemResponse(item)).willReturn(response);

            // when
            Page<ItemResponse> result = itemService.getAll(pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves and returns created item when not exists")
        void savesAndReturnsCreatedItemWhenNotExists() {
            // given
            CreateItemRequest request = new CreateItemRequest(NAME, PRICE);

            Item toSave = new Item();
            toSave.setName(NAME);
            toSave.setPrice(PRICE);

            Item saved = new Item();
            saved.setId(ID);
            saved.setName(NAME);
            saved.setPrice(PRICE);

            ItemResponse response = new ItemResponse(ID, NAME, PRICE);

            given(itemRepository.existsByNameAndPrice(NAME, PRICE)).willReturn(false);
            given(itemMapper.toItem(request)).willReturn(toSave);
            given(itemRepository.save(toSave)).willReturn(saved);
            given(itemMapper.toItemResponse(saved)).willReturn(response);

            // when
            ItemResponse result = itemService.create(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);
            verify(itemRepository).save(itemCaptor.capture());
            Item captured = itemCaptor.getValue();
            assertThat(captured.getName()).isEqualTo(NAME);
            assertThat(captured.getPrice()).isEqualTo(PRICE);
        }

        @Test
        @DisplayName("throws ItemAlreadyExistsException when item with same name and price exists")
        void throwsWhenItemAlreadyExists() {
            // given
            CreateItemRequest request = new CreateItemRequest(NAME, PRICE);
            given(itemRepository.existsByNameAndPrice(NAME, PRICE)).willReturn(true);

            // when / then
            assertThatThrownBy(() -> itemService.create(request))
                    .isInstanceOf(ItemAlreadyExistsException.class)
                    .hasMessageContaining(NAME);
            verify(itemRepository, never()).save(org.mockito.ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes when item exists")
        void deletesWhenItemExists() {
            // given
            given(itemRepository.existsById(ID)).willReturn(true);

            // when
            itemService.delete(ID);

            // then
            verify(itemRepository).deleteById(ID);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when item does not exist")
        void throwsWhenItemDoesNotExist() {
            // given
            given(itemRepository.existsById(ID)).willReturn(false);

            // when / then
            assertThatThrownBy(() -> itemService.delete(ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Item with id " + ID);
            verify(itemRepository, never()).deleteById(ID);
        }
    }
}

