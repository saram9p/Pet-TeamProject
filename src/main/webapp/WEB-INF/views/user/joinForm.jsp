<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../layout/header.jsp"%>
<style>
input[type="radio"]{width : 18px;}

</style>


<div class="container">
<h2>회원가입</h2>
  <p></p>
  <form action="" method="">
    <div class="form-group">
      <label for="uname">아이디</label>
      <input type="text" class="form-control" id="uname" placeholder="아이디를 입력하세요"  style="width:600px;" name="uname" required >
    </div>
     <br>
    <div class="form-group">
      <label for="pwd">비밀번호</label>
      <input type="password" class="form-control" id="pwd" placeholder="패스워드를 입력하세요"  style="width:600px;" name="pswd" required>
     </div>
      <br>
 <div class="form-group">
      <label for="pwd">이메일</label>
      <input type="email" class="form-control" id="email"  mexlength="30" placeholder="이메일을 입력하세요"  style="width:600px;" name="email" required>
     </div>
    <br>
     <label for="pwd">성별</label><br>
    <div class="form-check-inline">
      <label class="form-check-label" for="check1">
        <input type="radio" class="form-check-input" id="man" name="gender" value="man">남성
      </label>
    </div>
    <div class="form-check-inline">
      <label class="form-check-label" for="check2">
        <input type="radio" class="form-check-input" id="woman" name="gender" value="woman">여성
      </label>
    </div>
    <br>
    <br>
     <label for="pwd">생년월일</label><br>
    <div class="from-data">
    <input type="date" value='2000-01-01'><br>
    </div>
    
    <p></p>
    <button type="submit" class="btn btn-primary">회원가입 완료</button>
  </form>
</div>


<%@ include file="../layout/footer.jsp"%>