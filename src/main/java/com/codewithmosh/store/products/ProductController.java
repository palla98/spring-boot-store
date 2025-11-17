package com.codewithmosh.store.products;

import com.codewithmosh.store.users.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<ProductDto> getProductsByCategory(
            @RequestParam(required = false, name = "categoryId") Byte categoryId
    ) {
        List <Product> products;
        if(categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        }
        else
            products = productRepository.findAll();

        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        var product = productRepository.findById(id).orElse(null);
       if(product == null) {
           return ResponseEntity.notFound().build();
       }
        return ResponseEntity.ok(productMapper.toDto(product));
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(
            @RequestBody ProductDto productDto
    )
    {
        var product = productMapper.toEntity(productDto);

        //bigogna vedere se la category esiste altrimenti non ha senso aggiungere un prodotto ad una categoria che non c'è:
        var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);

        if(category == null) {
            return ResponseEntity.badRequest().build();
        }
        //facciamo la set perchè nel toEntity di prima nell implementazione non viene settata:
        product.setCategory(category);

        productRepository.save(product);
        return ResponseEntity.
                status(HttpStatus.CREATED).
                body(productMapper.toDto(product)); //potevo passargli il DTO della request ma mi sarei trovato l id a null
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @RequestBody ProductDto productDto,
            @PathVariable Long id
    )
    {
            var product =  productRepository.findById(id).orElse(null);
            if(product == null) {
                return ResponseEntity.notFound().build();
            }
            var category =  categoryRepository.findById(productDto.getCategoryId()).orElse(null);
            if(category == null) {
                return ResponseEntity.badRequest().build();
            }
            product.setCategory(category);
            productMapper.update(productDto, product);
            productRepository.save(product);
            return ResponseEntity.ok(productMapper.toDto(product));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id
    )
    {
        var product = productRepository.findById(id).orElse(null);
        if(product == null) {
            return ResponseEntity.notFound().build();
        }
        productRepository.delete(product);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



}
