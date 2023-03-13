package pl.coderslab.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.*;
import pl.coderslab.services.MarketService;
import pl.coderslab.repository.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class MarketController {

    private final BuyOrdersRepository buyOrdersRepository;
    private final SalesOrdersRepository salesOrdersRepository;
    private final CompaniesRepository companiesRepository;
    private final UserRepository userRepository;
    private final SharesHeldRepository sharesHeldRepository;
    private final MarketService marketService;


    public MarketController(BuyOrdersRepository buyOrdersRepository, SalesOrdersRepository salesOrdersRepository, CompaniesRepository companiesRepository, UserRepository userRepository, SharesHeldRepository sharesHeldRepository, MarketService marketService) {
        this.buyOrdersRepository = buyOrdersRepository;
        this.salesOrdersRepository = salesOrdersRepository;
        this.companiesRepository = companiesRepository;
        this.userRepository = userRepository;
        this.sharesHeldRepository = sharesHeldRepository;
        this.marketService = marketService;
    }

    @RequestMapping("/market")
    public String showMarket(Model model){
       // *********************BestBuyOrders************************************************

        List<BuyOrders> allBestBuyOrders = marketService.getBuyOrders();
        // allBestBuyOrders.forEach(System.out::println);

        // *********************BestSalesOrders************************************************

        List<SalesOrders> allBestSalesOrders = marketService.getSalesOrders();

        model.addAttribute("buyOrders", allBestBuyOrders);
        model.addAttribute("salesOrders", allBestSalesOrders);
        return "/market/market";
    }


    @RequestMapping("/market/all")
    public String showMarketAll(Model model){
        // *********************BestBuyOrders************************************************

        List<BuyOrders> allBuyOrders = buyOrdersRepository.findAll();
        // allBestBuyOrders.forEach(System.out::println);

        // *********************BestSalesOrders************************************************

        List<SalesOrders> allSalesOrders = salesOrdersRepository.findAll();

        model.addAttribute("buyOrders", allBuyOrders);
        model.addAttribute("salesOrders", allSalesOrders);
        return "/market/all";
    }




    @RequestMapping("/sell/{userId}/{companyId}/{volumen}/{priceLimit}")
    public String sellFromMarket(@PathVariable Long userId,
                                @PathVariable Long companyId,
                                @PathVariable int volumen,
                                @PathVariable double priceLimit, Model model){
        Companies company = companiesRepository.findById(companyId).get();
        System.out.println("*********************************************************************************************");
        System.out.println(company.getName());
        BuyOrders buyOrder = buyOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId);
        System.out.println(buyOrder.getId());
        buyOrder.setCompany(company);
        buyOrder.setVolumen(volumen);
        buyOrder.setPriceLimit(priceLimit);
        model.addAttribute("buyOrders", buyOrder);
        return "market/sell";
    }



    @PostMapping("/sell/{userId}/{companyName}/{volumen}/{priceLimit}")
    public String sellFromMarket(@Valid BuyOrders buyOrders, BindingResult result, @RequestParam String yourId, @RequestParam int volumen, Model model){
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




    @RequestMapping("/buy/{userId}/{companyId}/{volumen}/{priceLimit}")
    public String buyFromMarket(@PathVariable Long userId,
                                @PathVariable Long companyId,
                                @PathVariable int volumen,
                                @PathVariable double priceLimit, Model model){
        Companies company = companiesRepository.findById(companyId).get();
        SalesOrders salesOrder = salesOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId);
        salesOrder.setCompany(company);
        salesOrder.setVolumen(volumen);
        salesOrder.setPriceLimit(priceLimit);
        model.addAttribute("salesOrders", salesOrder);

        return "market/buy";
    }

    @PostMapping("/buy/{userId}/{companyName}/{volumen}/{priceLimit}")
    public String buyFromMarket(@ModelAttribute("salesOrders") @Valid SalesOrders salesOrder, BindingResult result, @RequestParam String yourId, @RequestParam int volumen, Model model){
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




}
