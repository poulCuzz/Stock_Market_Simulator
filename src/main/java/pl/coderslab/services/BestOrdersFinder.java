package pl.coderslab.services;

import lombok.Getter;
import lombok.Setter;
import pl.coderslab.BuyOrders;
import pl.coderslab.SalesOrders;
import pl.coderslab.repository.BuyOrdersRepository;
import pl.coderslab.repository.SalesOrdersRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class BestOrdersFinder {
    static BuyOrdersRepository buyOrdersRepository;
    static SalesOrdersRepository salesOrdersRepository;


    public void findAllCompaniesId(){
        salesOrdersRepository.findAll().forEach(System.out::println);
        List<BuyOrders> allBuyOrders = buyOrdersRepository.findAll();
        List<Long> allCompaniesIds = new ArrayList<>();

        for (BuyOrders b: allBuyOrders) {
            allCompaniesIds.add(b.getCompany().getId());
        }
        allCompaniesIds.forEach(System.out::println);
        List<Long> sortedList = new ArrayList<>();
        sortedList = allCompaniesIds.stream().sorted().distinct().collect(Collectors.toList());
        sortedList.forEach(System.out::println);
    }

    public static void main(String[] args) {
        BestOrdersFinder bestOrdersFinder = new BestOrdersFinder();
        bestOrdersFinder.findAllCompaniesId();
    }





}
