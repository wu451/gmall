package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
public class CartController {
    @Autowired
    private CartService cartService;
    @GetMapping("check/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId")Long userId){
     List<Cart> carts = this.cartService.queryCheckedCartsByUserId(userId);
     return ResponseVo.ok(carts);
    }
     @GetMapping
     public String addCart(Cart cart){
         this.cartService.addCart(cart);
         return "redirect:http://cart.gmall.com/addToCart.html?skuId="+cart.getSkuId();
     }
     @GetMapping("addToCart.html")
     public String addToCart(@RequestParam("skuId")Long skuId, Model model){
         Cart cart = this.cartService.queryCartBySkuId(skuId);
         model.addAttribute("cart", cart);
         return "addCart";
     }
    @GetMapping("test")
    @ResponseBody
    public String test(){
        //System.out.println(request.getAttribute("userId"));
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        System.out.println(userInfo.toString());
        return "hello!";
    }

    @GetMapping("cart.html")
    public String queryCarts(Model model){
       List<Cart>carts = this.cartService.queryCarts();
       model.addAttribute("carts", carts);
       return "cart";
    }
    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId")Long skuId){
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

}
