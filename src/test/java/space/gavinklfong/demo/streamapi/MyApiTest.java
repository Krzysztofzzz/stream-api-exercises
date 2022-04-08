package space.gavinklfong.demo.streamapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import space.gavinklfong.demo.streamapi.models.Order;
import space.gavinklfong.demo.streamapi.models.Product;
import space.gavinklfong.demo.streamapi.repos.CustomerRepo;
import space.gavinklfong.demo.streamapi.repos.OrderRepo;
import space.gavinklfong.demo.streamapi.repos.ProductRepo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@DataJpaTest
public class MyApiTest {

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Test
    @DisplayName("Obtain a list of product with category = \"Books\" and price > 100")
    void excercise1() {
        List<Product> productList = productRepo.findAll()
                .stream()
                .filter(s -> s.getCategory().equalsIgnoreCase("Books"))
                .filter(s -> s.getPrice() > 100)
                .collect(Collectors.toList());
        productList.forEach(System.out::println);
    }

    @Test
    @DisplayName("Obtain a list of product with category = \"Books\" and price > 100 (using Predicate chaining for filter)")
    public void exercise1a() {
        Predicate<Product> filterBooks = product -> product.getCategory().equalsIgnoreCase("books");
        Predicate<Product> filterPrice = product -> product.getPrice() > 100;
        List<Product> productList = productRepo.findAll()
                .stream()
                .filter(product -> filterBooks.and(filterPrice).test(product))
                .collect(Collectors.toList());
        productList.forEach(System.out::println);
    }

    @Test
    @DisplayName("Obtain a list of product with category = \"Books\" and price > 100 (using BiPredicate for filter)")
    public void exercise1b() {
        BiPredicate<Product, String> categoryFilter = (product, category) -> product.getCategory().equalsIgnoreCase(category);
        BiPredicate<Product, Integer> priceFilter = ((product, price) -> product.getPrice() > price);
        List<Product> productList = productRepo.findAll().stream()
                .filter(product -> categoryFilter.test(product, "books") && priceFilter.test(product, 100))
                .collect(Collectors.toList());
        productList.forEach(System.out::println);
    }

    @Test
    @DisplayName("Obtain a list of order with product category = \"Baby\"")
    public void exercise2() {
        List<Order> orderList = orderRepo.findAll().stream()
                .filter(order -> order.getProducts().stream()
                        .anyMatch(product -> product.getCategory().equalsIgnoreCase("baby")))
                .collect(Collectors.toList());
        orderList.forEach(System.out::println);
    }

    @Test
    @DisplayName("Obtain a list of product with category = ?Toys? and then apply 10% discount\"")
    public void exercise3() {
        List<Product> productList = productRepo.findAll().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase("toys"))
                .map(product -> product.withPrice(product.getPrice() * 0.9))
                .collect(Collectors.toList());
        productList.forEach(System.out::println);
    }

    @Test
    @DisplayName("Obtain a list of products ordered by customer of tier 2 between 01-Feb-2021 and 01-Apr-2021")
    public void exercise4() {
        List<Product> productList = orderRepo.findAll().stream()
                .filter(order -> order.getCustomer().getTier() == 2)
                .filter(order -> order.getOrderDate().compareTo(LocalDate.of(2021, 02, 01)) >= 0)
                .filter(order -> order.getOrderDate().compareTo(LocalDate.of(2021, 04, 01)) <= 0)
                .flatMap(order -> order.getProducts().stream())
                .distinct()
                .collect(Collectors.toList());
        productList.forEach(System.out::println);
    }

    @Test
    @DisplayName("Get the 3 cheapest products of \"Books\" category")
    public void exercise5() {
        List<Product> productList = productRepo.findAll().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase("books"))
                .sorted(Comparator.comparing(Product::getPrice))
                .limit(3)
                .collect(Collectors.toList());
        productList.forEach(System.out::println);
    }

}
