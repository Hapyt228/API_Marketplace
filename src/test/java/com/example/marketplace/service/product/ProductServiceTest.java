package com.example.marketplace.service.product;

import com.example.marketplace.dto.product.ProductRequest;
import com.example.marketplace.entity.product.Product;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.repository.category.CategoryRepository;
import com.example.marketplace.repository.product.ProductRepository;
import com.example.marketplace.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ProductService productService;

    private Product productOwnedBy(long sellerId) {
        User seller = mock(User.class);
        when(seller.getId()).thenReturn(sellerId);
        Product p = mock(Product.class);
        when(p.getSeller()).thenReturn(seller);
        return p;
    }

    @Test
    void updateProduct_foreignUser_throwsAccessDenied() {
        Product product = productOwnedBy(1L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProduct(5L, new ProductRequest(), 2L))
                .isInstanceOf(AccessDeniedException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_foreignUser_throwsAccessDenied() {
        Product product = productOwnedBy(1L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.deleteProduct(5L, 2L))
                .isInstanceOf(AccessDeniedException.class);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void deleteProduct_owner_deletes() {
        Product p = productOwnedBy(1L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(p));

        productService.deleteProduct(5L, 1L);

        verify(productRepository).delete(p);
    }

    @Test
    void deleteProduct_unknownId_throwsNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(5L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}