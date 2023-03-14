package pl.coderslab.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.*;
import pl.coderslab.services.MarketService;
import pl.coderslab.repository.*;

import javax.validation.Valid;
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

        List<BuyOrders> allBuyOrders = buyOrdersRepository.findAll();

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
        BuyOrders buyOrder = buyOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId);
        buyOrder.setCompany(company);
        buyOrder.setVolumen(volumen);
        buyOrder.setPriceLimit(priceLimit);
        model.addAttribute("buyOrders", buyOrder);
        return "market/sell";
    }



    @PostMapping("/sell/{userId}/{companyName}/{volumen}/{priceLimit}")
    public String sellFromMarket(@Valid BuyOrders buyOrders, BindingResult result, @RequestParam String yourId, @RequestParam int volumen, Model model){
        return marketService.sellFromMarket(buyOrders, result, yourId, volumen, model);
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
        return marketService.buyFromMarket(salesOrder, result, yourId, volumen, model);
    }




}
