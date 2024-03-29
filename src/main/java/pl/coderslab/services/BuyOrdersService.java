package pl.coderslab.services;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import pl.coderslab.BuyOrders;
import pl.coderslab.SalesOrders;
import pl.coderslab.User;
import pl.coderslab.repository.BuyOrdersRepository;
import pl.coderslab.repository.SalesOrdersRepository;
import pl.coderslab.repository.SharesHeldRepository;
import pl.coderslab.repository.UserRepository;

import java.util.List;

@Service
public class BuyOrdersService {
    private final BuyOrdersRepository buyOrdersRepository;
    private final SharesHeldRepository sharesHeldRepository;
    private final MarketService marketService;
    private final SalesOrdersRepository salesOrdersRepository;
    private final UserRepository userRepository;


    public BuyOrdersService(BuyOrdersRepository buyOrdersRepository, SharesHeldRepository sharesHeldRepository, MarketService marketService, SalesOrdersRepository salesOrdersRepository, UserRepository userRepository) {
        this.buyOrdersRepository = buyOrdersRepository;
        this.sharesHeldRepository = sharesHeldRepository;
        this.marketService = marketService;
        this.salesOrdersRepository = salesOrdersRepository;
        this.userRepository = userRepository;
    }

    public String editBuyOrder (BuyOrders buyOrder, BindingResult result, Model model) {
        if(result.hasErrors()){
            model.addAttribute("buyOrder", buyOrder);
            return "buyOrder/add";
        }
        BuyOrders buyOrderMain = buyOrdersRepository.findFirstByUser_IdAndCompany_Id(buyOrder.getUser().getId(), buyOrder.getCompany().getId());
        Long userId = buyOrder.getUser().getId();
        Long companyId = buyOrder.getCompany().getId();
        User user = userRepository.findById(userId).get();
        SalesOrders salesOrder = new SalesOrders();
        List<SalesOrders> list = marketService.getSalesOrders();
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getCompany().equals(buyOrder.getCompany())) {
                salesOrder = list.get(i);
            }else {
                salesOrder = null;
            }
        }
        if(user.getMoneyUsd() < buyOrder.getPriceLimit() * buyOrder.getVolumen()) {
            model.addAttribute("errorMessage", "You don't have enough funds to buy these shares!");
            model.addAttribute("buyOrder", buyOrder);
            return "buyOrder/add";
        }

        try{
            if(salesOrder.getUser() != null){
                if(buyOrder.getPriceLimit() >= salesOrder.getPriceLimit()) {
                    model.addAttribute("errorMessage", "There is already an order on the stock exchange that matches your price limit!");
                    return "redirect:/market";
                }
            }
        }
        catch (NullPointerException e) {
            model.addAttribute("errorMessage", "There is no matching announcement on the stock exchange!");
            e.printStackTrace();
        }


        double valueOfBuyOrderMain = buyOrderMain.getPriceLimit()* buyOrderMain.getVolumen();
        double valueOfBuyOrder = buyOrder.getPriceLimit()* buyOrder.getVolumen();
        if(valueOfBuyOrderMain != valueOfBuyOrder){
            user.setMoneyUsd(user.getMoneyUsd() + (valueOfBuyOrderMain - valueOfBuyOrder));
            userRepository.save(user);
        }

        BuyOrders editedBuyOrder = buyOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId);
        editedBuyOrder.setVolumen(buyOrder.getVolumen());
        editedBuyOrder.setCompany(buyOrder.getCompany());
        editedBuyOrder.setUser(buyOrder.getUser());
        editedBuyOrder.setPriceLimit(buyOrder.getPriceLimit());
        buyOrdersRepository.save(editedBuyOrder);
        return "redirect:/market";
    }

    public String delete(Long userId, Long companyId) {
        BuyOrders buyOrder = buyOrdersRepository.findFirstByUser_IdAndCompany_Id(userId, companyId);
        User user = userRepository.findById(userId).get();
        user.setMoneyUsd(buyOrder.getUser().getMoneyUsd() + buyOrder.getPriceLimit() * buyOrder.getVolumen());
        userRepository.save(user);
        buyOrdersRepository.delete(buyOrder);
        return "redirect:/market";
    }
}
