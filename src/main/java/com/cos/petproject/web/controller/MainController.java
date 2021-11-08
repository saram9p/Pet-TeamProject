package com.cos.petproject.web.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cos.petproject.domain.boast.Boast;
import com.cos.petproject.domain.boast.BoastRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class MainController {
	
	private final BoastRepository boastRepository;
	
	
	@GetMapping("/main")
	public String home(Model model) {
	//	int n = 3;
		
		// 전체 게시글 랭킹
	 //   List<Boast> mainAllRank = boastRepository.mMain();	
		
	//    List<Boast> mainThreeRank = 
	    
	 //   model.addAttribute("mainEntity", mainEntity);

		//System.out.println(model);
		
		
		return "main/main";
	}
}
