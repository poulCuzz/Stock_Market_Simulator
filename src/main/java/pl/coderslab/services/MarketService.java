package pl.coderslab.services;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import pl.coderslab.*;
import pl.coderslab.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketService {

    private final BuyOrdersRepository buyOrdersRepository;
    private final SalesOrdersRepository salesOrdersRepository;
    private final UserRepository userRepository;
    private final CompaniesRepository companiesRepository;
    private final SharesHeldRepository sharesHeldRepository;

    public MarketService(BuyOrdersRepository buyOrdersRepository, SalesOrdersRepository salesOrdersRepository, UserRepository userRepository, CompaniesRepository companiesRepository, SharesHeldRepository sharesHeldRepository) {
        this.buyOrdersRepository = buyOrdersRepository;
        this.salesOrdersRepository = salesOrdersRepository;
        this.userRepository = userRepository;
        this.companiesRepository = companiesRepository;
        this.sharesHeldRepository = sharesHeldRepository;
    }

    public List<BuyOrders> getBuyOrders() {
        List<BuyOrders> allBuyOrders = buyOrdersRepository.findAll();
        List<Long> allCompaniesIds = new ArrayList<>();
        for (BuyOrders b: allBuyOrders) {
            allCompaniesIds.add(b.getCompany().getId());
        }
        List<Long> sortedList = new ArrayList<>();
        sortedList = allCompaniesIds.stream()
                .sorted()
                .distinct()
                .collect(Collectors.toList());
        //  sortedList.forEach(System.out::println);
        List<BuyOrders> allBestBuyOrders = new ArrayList<>();
        for (int i = 0; i < sortedList.size(); i++) {
            List<BuyOrders> list = buyOrdersRepository.findByCompanyId(sortedList.get(i));
            if(!list.isEmpty()){
                List<BuyOrders> resultList = list.stream()
                        .sorted(Comparator.comparingDouble(BuyOrders::getPriceLimit).reversed())
                        .collect(Collectors.toList());
                BuyOrders result = resultList.get(0);
                allBestBuyOrders.add(result);
            }else {
                System.out.println("nie ma takich zleceń");
            }
        }
        return allBestBuyOrders;
    }


    public List<SalesOrders> getSalesOrders() {
        List<SalesOrders> allSalesOrders = salesOrdersRepository.findAll();
        List<Long> allCompaniesIds2 = new ArrayList<>();
        for (SalesOrders b: allSalesOrders) {
            allCompaniesIds2.add(b.getCompany().getId());
        }
        List<Long> sortedList2 = new ArrayList<>();
        sortedList2 = allCompaniesIds2.stream()
                .sorted()
                .distinct()
                .collect(Collectors.toList());
        //  sortedList2.forEach(System.out::println);
        List<SalesOrders> allBestSalesOrders = new ArrayList<>();
        for (int i = 0; i < sortedList2.size(); i++) {
            List<SalesOrders> list2 = salesOrdersRepository.findByCompanyId(sortedList2.get(i));
            if(!list2.isEmpty()){
                List<SalesOrders> resultList2 = list2.stream()
                        .sorted(Comparator.comparingDouble(SalesOrders::getPriceLimit))
                        .collect(Collectors.toList());
                SalesOrders result2 = resultList2.get(0);
                // System.out.println(result2);
                allBestSalesOrders.add(result2);
            }else {
                System.out.println("nie ma takich zleceń");
            }
        }
        return allBestSalesOrders;
    }



    public String buyFromMarket (SalesOrders salesOrder, BindingResult result, String yourId, int volumen, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("salesOrders", salesOrder);
            model.addAttribute("errorMessage", "we're sorry, something went wrong");
            System.out.println("!!masz jakiś błąd !!!");
            return "market/buy";
        }
        Long userId = salesOrder.getUser().getId();
        Long yourIdLong = Long.parseLong(yourId);
        User user = userRepository.findById(userId).get();
        User your = userRepository.findById(yourIdLong).get();
        Long usersCompanyId = salesOrder.getCompany().getId();
        Companies company = companiesRepository.findById(usersCompanyId).get();
        SalesOrders usersSalesOrder = salesOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, usersCompanyId);
        LocalDateTime localDateTime = LocalDateTime.now();
        String dateAndTime = localDateTime.toString().replaceAll("\\..*", "").replaceFirst("T", "  ");
        double usersPriceLimit = usersSalesOrder.getPriceLimit();
        if(yourIdLong == userId) {
            model.addAttribute("errorMessage", "You cannot execute transactions with yourself!");
            model.addAttribute("salesOrders", salesOrder);
            return "market/buy";
        }
        if(userRepository.findById(yourIdLong).get().getMoneyUsd() < salesOrder.getPriceLimit()*volumen) {
            model.addAttribute("errorMessage", "You don't have enough funds!");
            model.addAttribute("salesOrders", salesOrder);
            return "market/buy";
        }else if(volumen > usersSalesOrder.getVolumen()) {
            model.addAttribute("errorMessage", "You cannot buy more shares than the seller is offering!");
            model.addAttribute("salesOrders", salesOrder);
            return "market/buy";
        }else if(salesOrder.getPriceLimit() > usersPriceLimit){
            model.addAttribute("errorMessage", "Shares are not bought at a higher price than the one offered on the market!");
            model.addAttribute("salesOrders", salesOrder);
            return "market/buy";
        }
        if(salesOrder.getPriceLimit() == usersPriceLimit && volumen == usersSalesOrder.getVolumen()) {
            user.setMoneyUsd(user.getMoneyUsd() + salesOrder.getPriceLimit()*volumen);
            your.setMoneyUsd(your.getMoneyUsd() - salesOrder.getPriceLimit()*volumen);
            userRepository.save(user);
            userRepository.save(your);
            salesOrdersRepository.delete(salesOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, usersCompanyId));
            SharesHeld sharesHeld = new SharesHeld();
            sharesHeld.setVolume(volumen);
            sharesHeld.setCompany(companiesRepository.findById(usersCompanyId).get());
            sharesHeld.setUser(your);
            sharesHeld.setDateAndTime(dateAndTime);
            sharesHeld.setPurchasePrice(usersPriceLimit);
            sharesHeld.setValueAll(company.getPricePerStock() * salesOrder.getVolumen());
            sharesHeld.setPurchasePriceAll();
            sharesHeld.setProfitOrLoss();
            sharesHeldRepository.save(sharesHeld);
            company.setPricePerStock(salesOrder.getPriceLimit());
            companiesRepository.save(company);
            List<SharesHeld> list = sharesHeldRepository.findAllByCompany_Id(usersCompanyId);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setValueAll(list.get(i).getVolume() * salesOrder.getPriceLimit());
                list.get(i).setPurchasePrice(list.get(i).getPurchasePrice());
                list.get(i).setPurchasePriceAll();
                list.get(i).setProfitOrLoss();
                sharesHeldRepository.save(list.get(i));
            }
            return "redirect:/held/list?userId=" + yourId;
        }else if(salesOrder.getPriceLimit() == usersPriceLimit && volumen < usersSalesOrder.getVolumen()) {
            user.setMoneyUsd(user.getMoneyUsd() + salesOrder.getPriceLimit()*volumen);
            your.setMoneyUsd(your.getMoneyUsd() - salesOrder.getPriceLimit()*volumen);
            userRepository.save(user);
            userRepository.save(your);
            SalesOrders salesOrder2 = salesOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, usersCompanyId);
            salesOrder2.setVolumen(salesOrder2.getVolumen() - volumen);
            salesOrdersRepository.save(salesOrder2);
            SharesHeld sharesHeld = new SharesHeld();
            sharesHeld.setVolume(volumen);
            sharesHeld.setCompany(companiesRepository.findById(usersCompanyId).get());
            sharesHeld.setUser(your);
            sharesHeld.setDateAndTime(dateAndTime);
            sharesHeld.setPurchasePrice(usersPriceLimit);
            sharesHeld.setValueAll(company.getPricePerStock() * volumen);
            sharesHeld.setPurchasePriceAll();
            sharesHeld.setProfitOrLoss();
            sharesHeldRepository.save(sharesHeld);
            company.setPricePerStock(salesOrder.getPriceLimit());
            companiesRepository.save(company);
            List<SharesHeld> list = sharesHeldRepository.findAllByCompany_Id(usersCompanyId);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setValueAll(list.get(i).getVolume() * salesOrder.getPriceLimit());
                list.get(i).setPurchasePrice(list.get(i).getPurchasePrice());
                list.get(i).setPurchasePriceAll();
                list.get(i).setProfitOrLoss();
                sharesHeldRepository.save(list.get(i));
            }
            return "redirect:/held/list?userId=" + yourId;
        }else if(salesOrder.getPriceLimit() < usersPriceLimit) {
            your.setMoneyUsd(your.getMoneyUsd() - salesOrder.getPriceLimit() * salesOrder.getVolumen());
            userRepository.save(your);
            BuyOrders buyOrders = new BuyOrders();
            buyOrders.setUser(your);
            buyOrders.setVolumen(volumen);
            buyOrders.setCompany(companiesRepository.findById(usersCompanyId).get());
            buyOrders.setPriceLimit(salesOrder.getPriceLimit());
            buyOrdersRepository.save(buyOrders);
            return "redirect:/market";
        }
        return "redirect:/market";
    }



    public String sellFromMarket (BuyOrders buyOrders, BindingResult result, String yourId, int volumen, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("buyOrders", buyOrders);
            model.addAttribute("errorMessage", "we're sorry, something went wrong");
            return "market/sell";
        }
        Long userId = buyOrders.getUser().getId();
        Long usersCompanyId = buyOrders.getCompany().getId();
        BuyOrders usersBuyOrder = buyOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, usersCompanyId);
        Companies company = companiesRepository.findById(usersCompanyId).get();
        LocalDateTime localDateTime = LocalDateTime.now();
        String dateAndTime = localDateTime.toString().replaceAll("\\..*", "").replaceFirst("T", "  ");
        double usersPriceLimit = usersBuyOrder.getPriceLimit();
        Long yourIdLong = Long.parseLong(yourId);
        User your = userRepository.findById(yourIdLong).get();
        if(yourIdLong == userId) {
            model.addAttribute("errorMessage", "You cannot execute transactions with yourself!");
            model.addAttribute("buyOrders", buyOrders);
            return "market/sell";
        }
        if(sharesHeldRepository.findFirstByUserIdAndCompanyId(yourIdLong, usersCompanyId) == null) {
            model.addAttribute("errorMessage", "You do not have these shares in your portfolio!");
            return "redirect:/market";
        }else if(volumen > usersBuyOrder.getVolumen()){
            model.addAttribute("buyOrders", buyOrders);
            model.addAttribute("errorMessage", "There are not that many shares!");
            return "market/sell";
        }else if(volumen > sharesHeldRepository.findFirstByUserIdAndCompanyId(yourIdLong, usersCompanyId).getVolume()){
            model.addAttribute("buyOrders", buyOrders);
            model.addAttribute("errorMessage", "You do not have enough shares in portfolio!");
            return "market/sell";
        }
        if (buyOrders.getPriceLimit() < usersPriceLimit) {
            model.addAttribute("errorMessage", "Shares are not sold at a lower price than the one offered on the market");
            model.addAttribute("buyOrders", buyOrders);
            return "market/sell";
        }else if(buyOrders.getPriceLimit() == usersPriceLimit && volumen == usersBuyOrder.getVolumen()) {
            buyOrdersRepository.delete(usersBuyOrder);
            your.setMoneyUsd(userRepository.findById(yourIdLong).get().getMoneyUsd() + usersPriceLimit * volumen);
            userRepository.save(your);
            SharesHeld sharesHeld = new SharesHeld();
            sharesHeld.setVolume(buyOrders.getVolumen());
            sharesHeld.setUser(userRepository.findById(userId).get());
            sharesHeld.setCompany(buyOrders.getCompany());
            sharesHeld.setDateAndTime(dateAndTime);
            sharesHeld.setPurchasePrice(usersPriceLimit);
            sharesHeld.setValueAll(company.getPricePerStock() * buyOrders.getVolumen());
            sharesHeld.setPurchasePriceAll();
            sharesHeld.setProfitOrLoss();
            sharesHeldRepository.save(sharesHeld);
            company.setPricePerStock(buyOrders.getPriceLimit());
            companiesRepository.save(company);
            List<SharesHeld> list = sharesHeldRepository.findAllByCompany_Id(usersCompanyId);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setValueAll(list.get(i).getVolume() * buyOrders.getPriceLimit());
                list.get(i).setPurchasePrice(list.get(i).getPurchasePrice());
                list.get(i).setPurchasePriceAll();
                list.get(i).setProfitOrLoss();
                sharesHeldRepository.save(list.get(i));
            }

            SharesHeld yourSharesHeld = sharesHeldRepository.findFirstByUserIdAndCompanyId(yourIdLong, usersCompanyId);
            if(yourSharesHeld.getVolume() == volumen) {
                sharesHeldRepository.delete(yourSharesHeld);
                return "redirect:/held/list?userId=" + yourId;
            }else{
                yourSharesHeld.setVolume(yourSharesHeld.getVolume() - volumen);
                yourSharesHeld.setValueAll(yourSharesHeld.getValueAll() - buyOrders.getCompany().getPricePerStock()* yourSharesHeld.getVolume());
                yourSharesHeld.setPurchasePrice(yourSharesHeld.getPurchasePrice());
                yourSharesHeld.setPurchasePriceAll();
                yourSharesHeld.setProfitOrLoss();
                sharesHeldRepository.save(yourSharesHeld);
                return "redirect:/held/list?userId=" + yourId;
            }

        }else if (buyOrders.getPriceLimit() == usersPriceLimit && volumen < usersBuyOrder.getVolumen()) {
            usersBuyOrder.setVolumen(usersBuyOrder.getVolumen() - volumen);

            your.setMoneyUsd(userRepository.findById(yourIdLong).get().getMoneyUsd() + usersPriceLimit * volumen);
            userRepository.save(your);
            SharesHeld sharesHeld = new SharesHeld();
            sharesHeld.setVolume(buyOrders.getVolumen());
            sharesHeld.setUser(userRepository.findById(userId).get());
            sharesHeld.setCompany(buyOrders.getCompany());
            sharesHeld.setDateAndTime(dateAndTime);
            sharesHeld.setPurchasePrice(usersPriceLimit);
            sharesHeld.setValueAll(company.getPricePerStock() * volumen);
            sharesHeld.setPurchasePriceAll();
            sharesHeld.setProfitOrLoss();
            sharesHeldRepository.save(sharesHeld);
            company.setPricePerStock(buyOrders.getPriceLimit());
            companiesRepository.save(company);
            List<SharesHeld> list = sharesHeldRepository.findAllByCompany_Id(usersCompanyId);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setValueAll(list.get(i).getVolume() * buyOrders.getPriceLimit());
                list.get(i).setPurchasePrice(list.get(i).getPurchasePrice());
                list.get(i).setPurchasePriceAll();
                list.get(i).setProfitOrLoss();
                sharesHeldRepository.save(list.get(i));
            }

            SharesHeld yourSharesHeld = sharesHeldRepository.findFirstByUserIdAndCompanyId(yourIdLong, usersCompanyId);
            if(yourSharesHeld.getVolume() == volumen) {
                sharesHeldRepository.delete(yourSharesHeld);
                return "redirect:/held/list?userId=" + yourId;
            }else{
                yourSharesHeld.setVolume(yourSharesHeld.getVolume() - volumen);
                yourSharesHeld.setValueAll(yourSharesHeld.getValueAll() - buyOrders.getCompany().getPricePerStock()* yourSharesHeld.getVolume());
                yourSharesHeld.setPurchasePrice(yourSharesHeld.getPurchasePrice());
                yourSharesHeld.setPurchasePriceAll();
                yourSharesHeld.setProfitOrLoss();
                sharesHeldRepository.save(yourSharesHeld);
                return "redirect:/held/list?userId=" + yourId;
            }


        }else if(buyOrders.getPriceLimit() > usersPriceLimit) {
            SharesHeld sharesHeld = sharesHeldRepository.findFirstByUserIdAndCompanyId(yourIdLong, usersCompanyId);
            SalesOrders salesOrders = new SalesOrders();
            salesOrders.setUser(userRepository.findById(yourIdLong).get());
            salesOrders.setCompany(buyOrders.getCompany());
            salesOrders.setVolumen(buyOrders.getVolumen());
            salesOrders.setPriceLimit(buyOrders.getPriceLimit());
            salesOrdersRepository.save(salesOrders);
            if(buyOrders.getVolumen() == sharesHeld.getVolume()) {
                sharesHeldRepository.delete(sharesHeld);
                return "redirect:/market";
            }
            sharesHeld.setVolume(sharesHeld.getVolume() - volumen);
            sharesHeld.setValueAll(sharesHeld.getValueAll() - sharesHeld.getCompany().getPricePerStock()* volumen);
            sharesHeld.setPurchasePrice(sharesHeld.getPurchasePrice());
            sharesHeld.setPurchasePriceAll();
            sharesHeld.setProfitOrLoss();
            sharesHeldRepository.save(sharesHeld);

            return "redirect:/market";
        }
        return "redirect:/market";
    }
}
