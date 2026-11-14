<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page language="java" import="security.*"%>
<%@ include file="include/basePath.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Vexus 管理后台登录</title>
<style type="text/css">
body {    
	min-height: 100%;
    width: 100%;
    background-color:#0b0716;
    overflow: hidden;
}
.l-box1{     position: fixed;
    top: 50%;
    left: 50%;
    margin-left: -210px;
    margin-top: -146px;}
.tit01{    
	font-size: 26px;
    color: #1890ff;
    margin: 0 auto 40px auto;
    text-align: center;
    font-weight: 700;}
.ip01{width:420px; margin:0 auto 30px auto; text-align:center;}
.ip03{ width:420px; height:50px; line-height:50px; border:0px; font-size:16px; text-indent:45px;border-radius:5px; color: #000;}
.ip-n{ background:url(image/n2.png) no-repeat 15px center #fff; background-size:25px;}
.ip-p{ background:url(image/p2.png) no-repeat 15px center #fff; background-size:25px;}
.bn01{ background:#1890ff; color:#fff; font:20px/30px Arial, Helvetica, sans-serif; padding:14px 0; width:420px; border:0; border-radius:5px;}
.footer{height: 40px;
    line-height: 40px;
    position: fixed;
    bottom: 0;
    width: 100%;
    text-align: center;
    color: #fff;
    font-family: Arial;
    font-size: 12px;
    letter-spacing: 1px;
}
.h-t{color: #ff4949;
    font-size: 12px;
    line-height: 1.4;
    padding-top: 4px;
	text-align: left;
	display:none;
}

.login-bg-video{position:fixed;top:0;left:0;width:100vw;height:100vh;object-fit:cover;z-index:-2;pointer-events:none;}
.login-bg-overlay{position:fixed;top:0;left:0;width:100vw;height:100vh;background:linear-gradient(120deg,rgba(8,5,22,.78),rgba(8,5,22,.35));z-index:-1;pointer-events:none;}
.l-box1{z-index:10;}
.footer{z-index:10;color:#fff;text-shadow:0 1px 3px rgba(0,0,0,.75);}
.tit01{color:#fff;text-shadow:0 2px 8px rgba(0,0,0,.6);}
.ip03{box-shadow:0 8px 24px rgba(0,0,0,.25);}
.bn01{box-shadow:0 8px 24px rgba(24,144,255,.35);}


/* clean video login: no extra card, only glass inputs */
.l-box1{
  top:50% !important;
  left:50% !important;
  margin-left:-210px !important;
  margin-top:-146px !important;
  padding:0 !important;
  background:none !important;
  border:0 !important;
  border-radius:0 !important;
  box-shadow:none !important;
  -webkit-backdrop-filter:none !important;
  backdrop-filter:none !important;
  transform:none !important;
  width:auto !important;
}
.l-box1::before{display:none !important;content:none !important;}
.footer{display:none !important;}
.tit01{color:#fff !important;text-shadow:0 2px 14px rgba(0,0,0,.55);letter-spacing:2px;}
.ip03{
  background-color:rgba(255,255,255,.80) !important;
  -webkit-backdrop-filter:blur(16px) saturate(150%);
  backdrop-filter:blur(16px) saturate(150%);
  border:1px solid rgba(255,255,255,.65) !important;
  border-radius:10px !important;
  color:#111 !important;
  box-shadow:0 10px 28px rgba(0,0,0,.25), inset 0 1px 0 rgba(255,255,255,.95) !important;
}
.ip-n{background:url(image/n2.png) no-repeat 15px center rgba(255,255,255,.80) !important;}
.ip-p{background:url(image/p2.png) no-repeat 15px center rgba(255,255,255,.80) !important;}
.ip03::placeholder{color:#6b7280 !important;}
.bn01{background:#1890ff !important;box-shadow:none !important;font-weight:normal !important;}


/* keep the video's original colors: no darkening/gradient overlay */
.login-bg-overlay{background:none !important;display:none !important;}

/* transparent liquid glass inputs/button */
.ip01{position:relative;}
.ip01::before{
  content:"";
  position:absolute;
  left:-2px; right:-2px; top:2px; bottom:2px;
  border-radius:16px;
  background:linear-gradient(120deg,rgba(90,160,255,.55),rgba(150,100,255,.45) 45%,rgba(60,220,210,.35));
  filter:blur(13px);
  opacity:.5;
  z-index:0;
  pointer-events:none;
  animation:vexusLiquid 6s ease-in-out infinite alternate;
}
@keyframes vexusLiquid{
  0%{opacity:.38;transform:translateX(-2px) scale(1);}
  100%{opacity:.62;transform:translateX(2px) scale(1.01);}
}
.ip03{
  position:relative;
  z-index:1;
  background-color:rgba(255,255,255,.06) !important;
  background-image:linear-gradient(135deg,rgba(255,255,255,.24),rgba(255,255,255,.04)) !important;
  -webkit-backdrop-filter:blur(20px) saturate(170%);
  backdrop-filter:blur(20px) saturate(170%);
  border:1px solid rgba(255,255,255,.40) !important;
  border-radius:14px !important;
  color:#fff !important;
  text-indent:48px !important;
  box-shadow:inset 0 1px 0 rgba(255,255,255,.48), inset 0 -1px 0 rgba(0,0,0,.18), 0 10px 30px rgba(0,0,0,.22) !important;
}
.ip-n{
  background-image:url(image/n2-white.png),linear-gradient(135deg,rgba(255,255,255,.24),rgba(255,255,255,.04)) !important;
  background-repeat:no-repeat,no-repeat !important;
  background-position:16px center,0 0 !important;
  background-size:25px 25px,100% 100% !important;
  background-color:transparent !important;
}
.ip-p{
  background-image:url(image/p2-white.png),linear-gradient(135deg,rgba(255,255,255,.24),rgba(255,255,255,.04)) !important;
  background-repeat:no-repeat,no-repeat !important;
  background-position:16px center,0 0 !important;
  background-size:25px 25px,100% 100% !important;
  background-color:transparent !important;
}
.ip03::placeholder{color:rgba(255,255,255,.78) !important;text-shadow:0 1px 2px rgba(0,0,0,.35);}
.bn01{
  background:linear-gradient(135deg,rgba(70,140,255,.36),rgba(140,90,255,.24)) !important;
  -webkit-backdrop-filter:blur(20px) saturate(170%);
  backdrop-filter:blur(20px) saturate(170%);
  border:1px solid rgba(255,255,255,.42) !important;
  border-radius:14px !important;
  color:#fff !important;
  font-weight:600 !important;
  letter-spacing:1px;
  box-shadow:inset 0 1px 0 rgba(255,255,255,.55), 0 14px 36px rgba(50,110,255,.35) !important;
}
</style>
</head>

<body>
<video class="login-bg-video" autoplay muted loop playsinline preload="auto">
  <source src="/video/admin-login.mp4" type="video/mp4">
</video>
<div class="login-bg-overlay"></div>

<div class="l-box1">
<form action="<%=basePath%>public/login.action"  onsubmit="return toVaild()"
							class="ng-pristine ng-invalid ng-touched" method="post">
  <div class="tit01">VEXUS ADMIN</div>
  <div class="ip01">
  	<input id="j_username" name="j_username" type="text" class="ip03 ip-n" placeholder="User name" />
  <div class="h-t ht-name">请输入您的账号</div>
  </div>
  <div class="ip01">
  	<input id="j_password" name="j_password" type="password" class="ip03 ip-p" placeholder="Password" />
  <div class="h-t ht-pwd">请输入您的密码</div>
  </div>
  <div class="ip01">
  	<input id="googleAuthCode" name="googleAuthCode" type="text" class="ip03 ip-p" placeholder="GoogleAuthCode"/>
  <div class="h-t ht-code">请输入谷歌验证码</div>
  
  </div>
  <div class="ip01"><input name="提交" type="submit" class="bn01" value="Log in"/></div>
  </form>
</div>
</body>
</html>
<input type="hidden" name="error" id="error" value="${error}" />

<script type="text/javascript" src="<%=basePath%>js/jquery.min.js"></script>
<script type='text/javascript'>

//初始化执行一次
setTimeout(function() {
  start();    
}, 100);

function start() {      
	var error = $("#error").val();
	if (null == error || "" == error) {
		$(".ht-code").hide();
      } else {
    	$(".ht-code").show();
    	$(".ht-code").html(error);  
    	return false;
    }
}

var userVal,passVal,codeVal

function toVaild(){

	
	userVal = $('#j_username').val()
	passVal = $('#j_password').val()
	codeVal = $('#googleAuthCode').val()
	
	if(!userVal && !passVal && !codeVal){
		$(".ht-name").show();
		$(".ht-pwd").show();
		$(".ht-code").show();
		return false;
	}
	if(!userVal){
		$(".ht-name").show();
	}
	if(!passVal){
		$(".ht-pwd").show();
	}
	if(!codeVal){
		$(".ht-code").show();
	}
	
	if(userVal){
		$(".ht-name").hide();
	}
	if(passVal){
		$(".ht-pwd").hide();
	}
	if(codeVal){
		$(".ht-code").hide();
	}
	
	if(!userVal || !passVal || !codeVal){
		return false;
	}
	
}

function clickSbbmit(e){
  e.preventDefault();
}
</script>

