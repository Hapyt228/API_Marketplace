package com.example.marketplace.service.product;

import com.example.marketplace.dto.product.ProductPageResponse;
import com.example.marketplace.dto.product.ProductRequest;
import com.example.marketplace.dto.product.ProductResponse;
import com.example.marketplace.entity.category.Category;
import com.example.marketplace.entity.product.Product;
import com.example.marketplace.entity.user.User;
import com.example.marketplace.exception.InvalidPageRequestException;
import com.example.marketplace.exception.InvalidRequestParameterException;
import com.example.marketplace.exception.ResourceNotFoundException;
import com.example.marketplace.repository.category.CategoryRepository;
import com.example.marketplace.repository.product.ProductRepository;
import com.example.marketplace.repository.review.ReviewRepository;
import com.example.marketplace.repository.review.ReviewStatistics;
import com.example.marketplace.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import jakarta.persistence.criteria.Predicate;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, UserRepository userRepository, ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found by id = " + id));
        return newProductResponse(product);
    }

    public ProductPageResponse getAllProduct(Integer page, Integer pageSize, String sortRequest, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String q){
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestParameterException(
                    "minPrice must be greater than or equal to 0"
            );
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestParameterException(
                    "maxPrice must be greater than or equal to 0"
            );
        }
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidRequestParameterException(
                    "minPrice must be less than or equal to maxPrice"
            );
        }
        Specification<Product> specification = (root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();

            if (categoryId != null) {
                conditions.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                conditions.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                conditions.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (q != null && !q.isBlank()) {
                String search = q.trim()
                        .toLowerCase(Locale.ROOT)
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_");

                conditions.add(cb.like(cb.lower(root.get("name")), "%" + search + "%", '!'));
            }

            return cb.and(conditions.toArray(new Predicate[0]));
        };
        Sort sort = parseSort(sortRequest);
        Pageable pageable = PageRequest.of(
                page,
                pageSize,
                sort
        );
        Page<Product> productPage = productRepository.findAll(specification, pageable);
        ProductPageResponse productPageResponse = new ProductPageResponse();
        List<ProductResponse> productPageResponses = new ArrayList<>();
        for (Product product : productPage) {
            productPageResponses.add(newProductResponse(product));
        }
        productPageResponse.setContent(productPageResponses);
        productPageResponse.setPage(productPage.getNumber());
        productPageResponse.setPageSize(productPage.getSize());
        productPageResponse.setTotalElement(productPage.getTotalElements());
        productPageResponse.setTotalPages(productPage.getTotalPages());
        return productPageResponse;
    }

    public ProductResponse addProduct(ProductRequest productRequest, Long userId){
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + productRequest.getCategoryId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(category);
        product.setSeller(user);

        return newProductResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest productRequest, Long currentUserId){
        Product productByUpdate = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found by id = " + id));

        if (!Objects.equals(productByUpdate.getSeller().getId(), currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this product");
        }

        productByUpdate.setName(productRequest.getName());
        productByUpdate.setDescription(productRequest.getDescription());
        productByUpdate.setPrice(productRequest.getPrice());
        if (!Objects.equals(productByUpdate.getCategory().getId(), productRequest.getCategoryId())){
            Category category = categoryRepository.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + productRequest.getCategoryId()));
            productByUpdate.setCategory(category);
        }

        return newProductResponse(productRepository.save(productByUpdate));
    }

    public void deleteProduct(Long id, Long currentUserId){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found by id = " + id));
        if (!Objects.equals(product.getSeller().getId(), currentUserId)) {
            throw new AccessDeniedException("You are not the owner of this product");
        }
        productRepository.delete(product);
    }

    private  ProductResponse newProductResponse(Product product){
        ProductResponse productResponse = new ProductResponse(product);
        ReviewStatistics reviewStatistics = reviewRepository.getStatisticsByProductId(product.getId());
        productResponse.setAverageRating(reviewStatistics.getAverageRating());
        productResponse.setReviewsCount(reviewStatistics.getReviewsCount());
        return productResponse;
    }

    private Sort parseSort(String sortRequest){
        Sort sort;
        String nameBySort;
        String nameByFilter;
        if(sortRequest == null){
            return Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        String[] parts = sortRequest.split(",", -1);
        nameByFilter = parts[0];
        if(parts.length > 2){
            throw new InvalidRequestParameterException("Invalid sort format. Expected field or field,asc|desc");
        }
        if(parts.length == 1){
            nameBySort = "asc";
        }else {
            nameBySort = parts[1];
        }
        if(!(nameByFilter.equals("name") || nameByFilter.equals("price") ||  nameByFilter.equals("createdAt"))){
            throw new InvalidRequestParameterException("Allowed sort fields: price, name, createdAt");
        }
        if(!(nameBySort.equals("asc") ||  nameBySort.equals("desc"))){
            throw new InvalidRequestParameterException("Sort direction must be asc or desc");
        }
        if(nameBySort.equals("asc")) {
            sort = Sort.by(Sort.Direction.ASC, nameByFilter).and(Sort.by(Sort.Direction.ASC, "id"));
        }else{
            sort = Sort.by(Sort.Direction.DESC, nameByFilter).and(Sort.by(Sort.Direction.ASC, "id"));
        }
        return sort;
    }

}
