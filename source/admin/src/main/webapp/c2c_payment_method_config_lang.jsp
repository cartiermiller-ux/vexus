<%@ page language="java" pageEncoding="utf-8" isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@ include file="include/pagetop.jsp"%>

<!DOCTYPE html>
<html>

<head>
<%@ include file="include/head.jsp"%>
</head>

<body>

	<%@ include file="include/loading.jsp"%>

	<!-- //////////////////////////////////////////////////////////////////////////// -->
	<!-- START CONTENT -->
	<div class="ifr-dody">

		<!-- //////////////////////////////////////////////////////////////////////////// -->
		<!-- START CONTAINER -->
		<div class="ifr-con">
			<h3>C2C支付方式模板</h3>
			
			<%@ include file="include/alert.jsp"%>
			
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			<!-- START queryForm -->
			<form action="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!list.action" method="post" id="queryForm">
				<input type="hidden" name="pageNo" id="pageNo" value="${pageNo}" />
			</form>
			<!-- END queryForm -->
			<!-- //////////////////////////////////////////////////////////////////////////// -->
			
			<div class="row">
				<div class="col-md-12 col-lg-12">
					<div class="panel panel-default">

						<div class="panel-title">
							支付方式模板 多语言配置
							<ul class="panel-tools">
								<li><a class="icon minimise-tool"><i class="fa fa-minus"></i></a></li>
								<li><a class="icon expand-tool"><i class="fa fa-expand"></i></a></li>
							</ul>
						</div>

						<div class="panel-body">
						
							<form class="form-horizontal" action="<%=basePath%>normal/adminC2cPaymentMethodConfigAction!toUpdateLanguage.action"
								method="post" name="mainForm" id="mainForm">
								
								<input type="hidden" name="id" id="id" value="${id}" />
								<input type="hidden" name="method_type_name" id="method_type_name" value="${method_type_name}" >
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label"></label>
									<div class="col-sm-8">
										<p class="ballon color1">
											1. 多语言格式： 语言和翻译“##”分隔，不同语言“&&“分隔，如  zh-CN##账户号&&en##Account Number											
											<br />2. 语种： ${languageIntro}	
										</p>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式图片</label>
									<div class="col-sm-3">
										<label for="fileName_img"> 
											<img width="40px" height="40px" src="<%=base%>wap/public/showimg!showImg.action?imagePath=${method_img}" />
										</label>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式类型</label>
									<div class="col-sm-2">
										<select id="method_type" name="method_type" class="form-control " disabled="disabled" >
											<c:forEach items="${methodTypeMap}" var="item">
												<option value="${item.key}" <c:if test="${method_type == item.key}">selected="true"</c:if> >${item.value}</option>
											</c:forEach>
										</select>
									</div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式名称</label>
									<div class="col-sm-8">
										<input id="method_name" name="method_name" class="form-control " readonly="readonly" placeholder="支付方式名称" value="${method_name}" />
									</div>
								</div>								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">支付方式名称<br />多语言</label>
									<div class="col-sm-8">
										<textarea id="method_name_lang_trans" name="method_name_lang_trans" class="form-control input-lg" placeholder="支付方式名称 多语言" rows="4" cols="20" >${method_name_lang_trans}</textarea>
									</div> 
									<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('method_name')" class="btn btn-default">保存</a> </div>
								</div>
								
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名1（必填）</label>
									<div class="col-sm-8">
										<input id="param_name1" name="param_name1" class="form-control " readonly="readonly" placeholder="参数名1" value="${param_name1}" />
									</div>
								</div>
								<div class="form-group">
									<label class="col-sm-2 control-label form-label">参数名1<br />多语言</label>
									<div class="col-sm-8">
										<textarea id="param_name1_lang_trans" name="param_name1_lang_trans" class="form-control input-lg" placeholder="参数名1 多语言" rows="4" cols="20" >${param_name1_lang_trans}</textarea>
									</div> 
									<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name1')" class="btn btn-default">保存</a> </div>
								</div>
								
								<div id="param2_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名2（选配）</label>
										<div class="col-sm-8">
											<input id="param_name2" name="param_name2" class="form-control " readonly="readonly" placeholder="参数名2" value="${param_name2}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名2<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name2_lang_trans" name="param_name2_lang_trans" class="form-control input-lg" placeholder="参数名2 多语言" rows="4" cols="20" >${param_name2_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name2')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param3_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名3（选配）</label>
										<div class="col-sm-8">
											<input id="param_name3" name="param_name3" class="form-control " readonly="readonly" placeholder="参数名3" value="${param_name3}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名3<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name3_lang_trans" name="param_name3_lang_trans" class="form-control input-lg" placeholder="参数名3 多语言" rows="4" cols="20" >${param_name3_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name3')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param4_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名4（选配）</label>
										<div class="col-sm-8">
											<input id="param_name4" name="param_name4" class="form-control " readonly="readonly" placeholder="参数名4" value="${param_name4}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名4<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name4_lang_trans" name="param_name4_lang_trans" class="form-control input-lg" placeholder="参数名4 多语言" rows="4" cols="20" >${param_name4_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name4')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param5_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名5（选配）</label>
										<div class="col-sm-8">
											<input id="param_name5" name="param_name5" class="form-control " readonly="readonly" placeholder="参数名5" value="${param_name5}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名5<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name5_lang_trans" name="param_name5_lang_trans" class="form-control input-lg" placeholder="参数名5 多语言" rows="4" cols="20" >${param_name5_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name5')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param6_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名6（选配）</label>
										<div class="col-sm-8">
											<input id="param_name6" name="param_name6" class="form-control " readonly="readonly" placeholder="参数名6" value="${param_name6}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名6<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name6_lang_trans" name="param_name6_lang_trans" class="form-control input-lg" placeholder="参数名6 多语言" rows="4" cols="20" >${param_name6_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name6')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param7_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名7（选配）</label>
										<div class="col-sm-8">
											<input id="param_name7" name="param_name7" class="form-control " readonly="readonly" placeholder="参数名7" value="${param_name7}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名7<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name7_lang_trans" name="param_name7_lang_trans" class="form-control input-lg" placeholder="参数名7 多语言" rows="4" cols="20" >${param_name7_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name7')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param8_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名8（选配）</label>
										<div class="col-sm-8">
											<input id="param_name8" name="param_name8" class="form-control " readonly="readonly" placeholder="参数名8" value="${param_name8}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名8<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name8_lang_trans" name="param_name8_lang_trans" class="form-control input-lg" placeholder="参数名8 多语言" rows="4" cols="20" >${param_name8_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name8')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param9_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名9（选配）</label>
										<div class="col-sm-8">
											<input id="param_name9" name="param_name9" class="form-control " readonly="readonly" placeholder="参数名9" value="${param_name9}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名9<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name9_lang_trans" name="param_name9_lang_trans" class="form-control input-lg" placeholder="参数名9 多语言" rows="4" cols="20" >${param_name9_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name9')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param10_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名10（选配）</label>
										<div class="col-sm-8">
											<input id="param_name10" name="param_name10" class="form-control " readonly="readonly" placeholder="参数名10" value="${param_name10}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名10<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name10_lang_trans" name="param_name10_lang_trans" class="form-control input-lg" placeholder="参数名10 多语言" rows="4" cols="20" >${param_name10_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name10')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param11_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名11（选配）</label>
										<div class="col-sm-8">
											<input id="param_name11" name="param_name11" class="form-control " readonly="readonly" placeholder="参数名11" value="${param_name11}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名11<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name11_lang_trans" name="param_name11_lang_trans" class="form-control input-lg" placeholder="参数名11 多语言" rows="4" cols="20" >${param_name11_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name11')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param12_div">									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名12（选配）</label>
										<div class="col-sm-8">
											<input id="param_name12" name="param_name12" class="form-control " readonly="readonly" placeholder="参数名12" value="${param_name12}" />
										</div>
									</div>								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名12<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name12_lang_trans" name="param_name12_lang_trans" class="form-control input-lg" placeholder="参数名12 多语言" rows="4" cols="20" >${param_name12_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name12')" class="btn btn-default">保存</a> </div>
									</div>									
								</div>
								
								<div id="param13_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名13（选配）</label>
										<div class="col-sm-8">
											<input id="param_name13" name="param_name13" class="form-control " readonly="readonly" placeholder="参数名13" value="${param_name13}" />
										</div>
									</div>									
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名13<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name13_lang_trans" name="param_name13_lang_trans" class="form-control input-lg" placeholder="参数名13 多语言" rows="4" cols="20" >${param_name13_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name13')" class="btn btn-default">保存</a> </div>
									</div>
								</div>
								
								<div id="param14_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名14（选配）</label>
										<div class="col-sm-8">
											<input id="param_name14" name="param_name14" class="form-control " readonly="readonly" placeholder="参数名14" value="${param_name14}" />
										</div>
									</div>								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名14<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name14_lang_trans" name="param_name14_lang_trans" class="form-control input-lg" placeholder="参数名14 多语言" rows="4" cols="20" >${param_name14_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name14')" class="btn btn-default">保存</a> </div>
									</div>
								</div>
								
								<div id="param15_div">								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名15（选配）</label>
										<div class="col-sm-8">
											<input id="param_name15" name="param_name15" class="form-control " readonly="readonly" placeholder="参数名15" value="${param_name15}" />
										</div>
									</div>								
									<div class="form-group">
										<label class="col-sm-2 control-label form-label">参数名15<br />多语言</label>
										<div class="col-sm-8">
											<textarea id="param_name15_lang_trans" name="param_name15_lang_trans" class="form-control input-lg" placeholder="参数名15 多语言" rows="4" cols="20" >${param_name15_lang_trans}</textarea>
										</div> 
										<div class="col-sm-2"> <a href="javascript:saveLangTransConfig('param_name15')" class="btn btn-default">保存</a> </div>
									</div>
								</div>
				                
								<div class="form-group">
									<div class="col-sm-offset-2 col-sm-10">
										<a href="javascript:goUrl(${pageNo})" class="btn">返回</a>
									</div>
								</div>
								
							</form>
							
						</div>

					</div>
				</div>
			</div>

		</div>
		<!-- END CONTAINER -->
		<!-- //////////////////////////////////////////////////////////////////////////// -->

		<%@ include file="include/footer.jsp"%>

	</div>
	<!-- End Content -->
	<!-- //////////////////////////////////////////////////////////////////////////// -->

	<%@ include file="include/js.jsp"%>
	
	<script src="<%=basePath%>js/util.js" type="text/javascript"></script>
	
	<script type="text/javascript">
	
		// 初始化执行一次
		setTimeout(function() {
			start();	  
		}, 100);
		
		function start() {
			var param_name2 = $("#param_name2").val();
			if (null != param_name2 && "" != param_name2) { $("#param2_div").show(); } else { $("#param2_div").hide(); }
			var param_name3 = $("#param_name3").val();
			if (null != param_name3 && "" != param_name3) { $("#param3_div").show(); } else { $("#param3_div").hide(); }
			var param_name4 = $("#param_name4").val();
			if (null != param_name4 && "" != param_name4) { $("#param4_div").show(); } else { $("#param4_div").hide(); }
			var param_name5 = $("#param_name5").val();
			if (null != param_name5 && "" != param_name5) { $("#param5_div").show(); } else { $("#param5_div").hide(); }
			var param_name6 = $("#param_name6").val();
			if (null != param_name6 && "" != param_name6) { $("#param6_div").show(); } else { $("#param6_div").hide(); }
			var param_name7 = $("#param_name7").val();
			if (null != param_name7 && "" != param_name7) { $("#param7_div").show(); } else { $("#param7_div").hide(); }
			var param_name8 = $("#param_name8").val();
			if (null != param_name8 && "" != param_name8) { $("#param8_div").show(); } else { $("#param8_div").hide(); }
			var param_name9 = $("#param_name9").val();
			if (null != param_name9 && "" != param_name9) { $("#param9_div").show(); } else { $("#param9_div").hide(); }
			var param_name10 = $("#param_name10").val();
			if (null != param_name10 && "" != param_name10) { $("#param10_div").show(); } else { $("#param10_div").hide(); }
			var param_name11 = $("#param_name11").val();
			if (null != param_name11 && "" != param_name11) { $("#param11_div").show(); } else { $("#param11_div").hide(); }
			var param_name12 = $("#param_name12").val();
			if (null != param_name12 && "" != param_name12) { $("#param12_div").show(); } else { $("#param12_div").hide(); }
			var param_name13 = $("#param_name13").val();
			if (null != param_name13 && "" != param_name13) { $("#param13_div").show(); } else { $("#param13_div").hide(); }
			var param_name14 = $("#param_name14").val();
			if (null != param_name14 && "" != param_name14) { $("#param14_div").show(); } else { $("#param14_div").hide(); }
			var param_name15 = $("#param_name15").val();
			if (null != param_name15 && "" != param_name15) { $("#param15_div").show(); } else { $("#param15_div").hide(); }
		}
		
		function saveLangTransConfig(content_name) {
			switch (content_name) {
				case "method_type": {saveLangTrans(content_name, $("#method_type_name").val(), $("#method_type_lang_trans").val());break;}
				case "method_name": {saveLangTrans(content_name, $("#method_name").val(), $("#method_name_lang_trans").val());break;}
				case "param_name1": {saveLangTrans(content_name, $("#param_name1").val(), $("#param_name1_lang_trans").val());break;}
				case "param_name2": {saveLangTrans(content_name, $("#param_name2").val(), $("#param_name2_lang_trans").val());break;}
				case "param_name3": {saveLangTrans(content_name, $("#param_name3").val(), $("#param_name3_lang_trans").val());break;}
				case "param_name4": {saveLangTrans(content_name, $("#param_name4").val(), $("#param_name4_lang_trans").val());break;}
				case "param_name5": {saveLangTrans(content_name, $("#param_name5").val(), $("#param_name5_lang_trans").val());break;}
				case "param_name6": {saveLangTrans(content_name, $("#param_name6").val(), $("#param_name6_lang_trans").val());break;}
				case "param_name7": {saveLangTrans(content_name, $("#param_name7").val(), $("#param_name7_lang_trans").val());break;}
				case "param_name8": {saveLangTrans(content_name, $("#param_name8").val(), $("#param_name8_lang_trans").val());break;}
				case "param_name9": {saveLangTrans(content_name, $("#param_name9").val(), $("#param_name9_lang_trans").val());break;}
				case "param_name10": {saveLangTrans(content_name, $("#param_name10").val(), $("#param_name10_lang_trans").val());break;}
				case "param_name11": {saveLangTrans(content_name, $("#param_name11").val(), $("#param_name11_lang_trans").val());break;}
				case "param_name12": {saveLangTrans(content_name, $("#param_name12").val(), $("#param_name12_lang_trans").val());break;}
				case "param_name13": {saveLangTrans(content_name, $("#param_name13").val(), $("#param_name13_lang_trans").val());break;}
				case "param_name14": {saveLangTrans(content_name, $("#param_name14").val(), $("#param_name14_lang_trans").val());break;}
				case "param_name15": {saveLangTrans(content_name, $("#param_name15").val(), $("#param_name15_lang_trans").val());break;}
				default: { }
			}
		}
		
		function saveLangTrans(content_name, content, lang_trans) {
			var url = "<%=basePath%>normal/adminC2cPaymentMethodConfigAction!saveTranslate.action";
			var data = 
			{
				"content_name": content_name,
				"content": content,
				"lang_trans": lang_trans
			};
			goNewAjaxUrl(url, data, function(tmp) {
				switch (tmp.content_name) {
					case "method_type": {$("#method_type_lang_trans").val(tmp.lang_trans);break;}
					case "method_name": {$("#method_name_lang_trans").val(tmp.lang_trans);break;}
					case "param_name1": {$("#param_name1_lang_trans").val(tmp.lang_trans);break;}
					case "param_name2": {$("#param_name2_lang_trans").val(tmp.lang_trans);break;}
					case "param_name3": {$("#param_name3_lang_trans").val(tmp.lang_trans);break;}
					case "param_name4": {$("#param_name4_lang_trans").val(tmp.lang_trans);break;}
					case "param_name5": {$("#param_name5_lang_trans").val(tmp.lang_trans);break;}
					case "param_name6": {$("#param_name6_lang_trans").val(tmp.lang_trans);break;}
					case "param_name7": {$("#param_name7_lang_trans").val(tmp.lang_trans);break;}
					case "param_name8": {$("#param_name8_lang_trans").val(tmp.lang_trans);break;}
					case "param_name9": {$("#param_name9_lang_trans").val(tmp.lang_trans);break;}
					case "param_name10": {$("#param_name10_lang_trans").val(tmp.lang_trans);break;}
					case "param_name11": {$("#param_name11_lang_trans").val(tmp.lang_trans);break;}
					case "param_name12": {$("#param_name12_lang_trans").val(tmp.lang_trans);break;}
					case "param_name13": {$("#param_name13_lang_trans").val(tmp.lang_trans);break;}
					case "param_name14": {$("#param_name14_lang_trans").val(tmp.lang_trans);break;}
					case "param_name15": {$("#param_name15_lang_trans").val(tmp.lang_trans);break;}
					default: { }
				}
			}, function() {
			});
		}
		
		function goNewAjaxUrl(targetUrl, data, Func, Fail) {
			$.ajax({
				url: targetUrl,
				data: data,
				type: 'get',
				dataType: "json",
				success: function (res) {
					// var tmp = $.parseJSON(res)
					var tmp = res;
				    if (tmp.code == 200) {
				    	Func(tmp);
				    	swal({
							title : "保存成功",
							text : "",
							type : "success",
							showCancelButton : false,
							confirmButtonColor : "#DD6B55",
							confirmButtonText : "确认",
							closeOnConfirm : false
						});
				    } else if (tmp.code == 500) {
				    	Fail();
				    	swal({
							title : tmp.message,
							text : "",
							type : "warning",
							showCancelButton : true,
							confirmButtonColor : "#DD6B55",
							confirmButtonText : "确认",
							closeOnConfirm : false
						});
				    }
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					swal({
						title : "请求错误",
						text : "",
						type : "warning",
						showCancelButton : true,
						confirmButtonColor : "#DD6B55",
						confirmButtonText : "确认",
						closeOnConfirm : false
					});
					console.log("请求错误");
				}
			});
		}
		
	</script>

</body>

</html>
