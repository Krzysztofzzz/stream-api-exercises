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
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
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

    @Test
    @DisplayName("Get the 3 most recent placed order")
    public void exercise6() {
        List<Order> orderList = orderRepo.findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(3)
                .collect(Collectors.toList());
        orderList.forEach(System.out::println);
    }

    @Test
    @DisplayName("Get a list of products which was ordered on 15-Mar-2021")
    public void exercise7() {
        List<Product> productList = productRepo.findAll().stream()
                .filter(product -> product.getOrders().stream()
                        .anyMatch(order -> order.getOrderDate().equals(LocalDate.of(2021, 03, 15))))
                .collect(Collectors.toList());
        productList.forEach(System.out::println);
    }

    @Test
    @DisplayName("Calculate the total lump of all orders placed in Feb 2021")
    public void exercise8() {
        LocalDate endOfJan = LocalDate.of(2021, 01, 31);
        LocalDate firstMarch = LocalDate.of(2021, 03, 01);
        Double sum = orderRepo.findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(endOfJan))
                .filter(order -> order.getOrderDate().isBefore(firstMarch))
                .flatMap(order -> order.getProducts().stream())
                .mapToDouble(Product::getPrice)
                .sum();
        System.out.println(sum);
    }

    @Test
    @DisplayName("Calculate the total lump of all orders placed in Feb 2021 (using reduce with BiFunction)")
    public void exercise8a() {
        BiFunction<Double, Product, Double> accumulator = (acc, product) -> acc + product.getPrice();
        LocalDate lastJan = LocalDate.of(2021, 1, 31);
        LocalDate firstMarch = LocalDate.of(2021, 3, 1);
        double sum = orderRepo.findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(lastJan))
                .filter(order -> order.getOrderDate().isBefore(firstMarch))
                .flatMap(order -> order.getProducts().stream())
                .reduce(0D, accumulator, Double::sum);
        System.out.println(sum);
    }

    @Test
    @DisplayName("Calculate the average price of all orders placed on 15-Mar-2021")
    public void exercise9() {
        double average = orderRepo.findAll().stream()
                .filter(order -> order.getOrderDate().equals(LocalDate.of(2021, 3, 15)))
                .flatMap(order -> order.getProducts().stream())
                .mapToDouble(Product::getPrice)
                .average()
                .getAsDouble();
        System.out.println("Average price: " + average);
    }

    @Test
    @DisplayName("Obtain statistics summary of all products belong to \"Books\" category")
    public void exercise10() {
        DoubleSummaryStatistics summaryStatistics = productRepo.findAll().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase("books"))
                .mapToDouble(Product::getPrice)
                .summaryStatistics();
        System.out.println("Number of values: " + summaryStatistics.getCount() + "\n" +
                "Sum of prices: " + summaryStatistics.getSum() + "\n" +
                "Minimum price: " + summaryStatistics.getMin() + "\n" +
                "Maximum price: " + summaryStatistics.getMax() + "\n" +
                "Average price: " + summaryStatistics.getAverage()
        );
    }

    @Test
    @DisplayName("Obtain a mapping of order id and the order's product count")
    public void exercise11() {
        Map<Long, Integer> mapping = orderRepo.findAll().stream()
                .collect(Collectors.toMap(
                        Order::getId,
                        order -> order.getProducts().size()
                ));
        System.out.println(mapping);
    }
}
