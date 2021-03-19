package com.ausclouds.bdbsec.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ausclouds.bdbsec.util.PasswordUtils;

@RestController
public class Test {
	
	@RequestMapping("/test")
    public String hello() {

           return "bey-bye";

    }
	
	public static void main(String[] args) throws Exception{
		String password = "t";
        //加密：
        String encryptPassword = PasswordUtils.encryptPassword(password);
        System.out.println("加密后："+ encryptPassword);
        //解密：
        String decryptPassword = PasswordUtils.decryptPassword(encryptPassword);
        System.out.println("解密后："+ decryptPassword);
	}

}
