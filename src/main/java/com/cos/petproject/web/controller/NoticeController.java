package com.cos.petproject.web.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cos.petproject.domain.animal.Animal;
import com.cos.petproject.domain.notice.Notice;
import com.cos.petproject.domain.notice.NoticeRepository;
import com.cos.petproject.domain.qna.Qna;
import com.cos.petproject.domain.user.User;
import com.cos.petproject.handler.exception.MyAsyncNotFoundException;
import com.cos.petproject.handler.exception.MyNotFoundException;
import com.cos.petproject.util.Script;
import com.cos.petproject.web.dto.CMRespDto;
import com.cos.petproject.web.dto.board.NoticeSaveReqDto;
import com.cos.petproject.web.dto.board.QnaSaveReqDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Controller
public class NoticeController {
	private final NoticeRepository noticeRepository;
	private final HttpSession session;
	
	
	// 글작성 기능---------------------------------
	@PostMapping("/notice")
	public @ResponseBody String save( @Valid NoticeSaveReqDto dto, BindingResult bindingResult) {
		
		User principal = (User) session.getAttribute("principal");

		// 인증
		if (principal == null) { // 로그인 안됨
			return Script.href("/user/loginForm", "잘못된 접근입니다");
		} 
		
		// 관리자인증
		if(!principal.getAuthority().equals("admin")) {
			return Script.href("/", "관리자만 접근 가능합니다.");
		}

		// 유효성 검사
		if (bindingResult.hasErrors()) {
			Map<String, String> errorMap = new HashMap<>();
			for (FieldError error : bindingResult.getFieldErrors()) {
				errorMap.put(error.getField(), error.getDefaultMessage());
				System.out.println("필드: " + error.getField());
				System.out.println("메시지: " + error.getDefaultMessage());
			}
			return Script.back(errorMap.toString());
		}

		// <p> 태그 제거
		dto.setContent(dto.getContent().replaceAll("<p>", ""));
		dto.setContent(dto.getContent().replaceAll("</p>", ""));

		// 글 작성
		noticeRepository.mSave(dto.getTitle(), dto.getContent(), principal);
		
		return Script.href("/notice?page=0");

	}
	
	// 글수정 기능---------------------------------
	@PutMapping("/notice/{id}")
	public @ResponseBody CMRespDto<String> update(@PathVariable int id, @RequestBody @Valid NoticeSaveReqDto dto, BindingResult bindingResult) {
		
		//유효성 검사(공통로직)
		if (bindingResult.hasErrors()) {
			Map<String, String> errorMap = new HashMap<>();
			for (FieldError error : bindingResult.getFieldErrors()) {
				errorMap.put(error.getField(), error.getDefaultMessage());
			}
			throw new MyAsyncNotFoundException(errorMap.toString());
		}

		//인증
		User principal = (User) session.getAttribute("principal");
		if(principal == null) {
			throw new MyAsyncNotFoundException("인증이 되지 않았습니다.");
		}
		Notice noticeEntity = noticeRepository.findById(id)
				.orElseThrow(()->new MyAsyncNotFoundException("해당 게시글을 찾을 수 없습니다"));
		
		if(principal.getId() != noticeEntity.getUser().getId()) {
			throw new MyAsyncNotFoundException("해당 게시물의 권한이 없습니다");
		}
		
		
		Notice notice = dto.toEntity(principal);
		notice.setUser(principal);
		notice.setId(id);
		notice.setCounter(noticeEntity.getCounter());
		notice.setCreatedAt(LocalDateTime.now());
		noticeRepository.save(notice);			
		
		return new CMRespDto<>(1, "업데이트 성공", null);
		

	}

	
	
	
	
	// 글삭제 기능---------------------------------
	@DeleteMapping("/notice/{id}")
	public @ResponseBody CMRespDto<String> delete(@PathVariable int id) {
	
		// 인증이 된 사람만 함수 접근 가능!! (로그인 된 사람)
		User principal = (User) session.getAttribute("principal");
		if (principal == null) {
			throw new MyAsyncNotFoundException("인증이 되지 않았습니다.");
		}

		// 권한이 있는 사람만 함수 접근 가능(principal.id == {id})
		Notice noticeEntity = noticeRepository.findById(id).orElseThrow(() -> new MyAsyncNotFoundException("해당글을 찾을 수 없습니다."));
		if (principal.getId() != noticeEntity.getUser().getId()) {
			throw new MyAsyncNotFoundException("해당글을 삭제할 권한이 없습니다.");
		}

		try {
			noticeRepository.deleteById(id); // 오류 발생??? (id가 없으면)
		} catch (Exception e) {
			throw new MyAsyncNotFoundException(id + "를 찾을 수 없어서 삭제할 수 없어요.");
		}

		return new CMRespDto<String>(1, "성공", null); // @ResponseBody 데이터 리턴!! String
	}
	
	
	
	// 페이지 불러오기
	@GetMapping("/notice/{id}/updateForm")
	public String noticeUpdateForm(@PathVariable int id, Model model) {
		
		Notice noticeEntity = noticeRepository.findById(id).orElseThrow(() -> new MyNotFoundException(id + " 페이지를 찾을 수 없습니다."));
		
		LocalDateTime boardCreatedAt = noticeEntity.getCreatedAt();
		String parseCreatedAt = boardCreatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		model.addAttribute("noticeEntity", noticeEntity);
		model.addAttribute("parseCreatedAt", parseCreatedAt);
		
		return "notice/updateForm";
	}

	@GetMapping("/notice/saveForm")
	public String noticeSaveForm() {
		
		return "notice/saveForm";
	}
	
	@GetMapping("/notice")
	public String home(@RequestParam int page, Model model) {
		
		Pageable pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());
		Page<Notice> noticeEntity = noticeRepository.findAll(pageRequest);
		int pageNumber = noticeEntity.getPageable().getPageNumber();
		int pageBlock = 10;
		int startBlockPage = ((pageNumber) / pageBlock) * pageBlock + 1;
		int endBlockPage = startBlockPage + pageBlock - 1;

		
		model.addAttribute("startBlockPage", startBlockPage);
		model.addAttribute("endBlockPage", endBlockPage);
		model.addAttribute("noticeEntity", noticeEntity);

		return "/notice/list";
	}
	
	
	
	@GetMapping("/notice/{id}")
	public String detail(@PathVariable int id, Model model) {
	
	// 게시판 조회수 증가
	noticeRepository.mCounter(id);

	// id로 게시글 찾기
	Notice noticeEntity = noticeRepository.findById(id).orElseThrow(() -> new MyNotFoundException(id + " 페이지를 찾을 수 없습니다."));

	// 날짜변환
	LocalDateTime boardCreatedAt = noticeEntity.getCreatedAt();
	String parseCreatedAt = boardCreatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

	// 모델에 담기
	model.addAttribute("noticeEntity", noticeEntity);
	model.addAttribute("parseCreatedAt", parseCreatedAt);

	return "notice/detail";
	}
}
