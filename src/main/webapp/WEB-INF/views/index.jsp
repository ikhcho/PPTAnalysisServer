<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
<script>
$(function(){
	//TF-IDF사전
	$('#tfidf').click(function(){
		var from = $('#tfidfFrom').val();
		var to = $('#tfidfTo').val();
		$.ajax({
			url : '${pageContext.request.contextPath }/dictionary/selectTFIDF.do?newsCode=economic&from=' + from + '&to=' + to,
			success : function(data){
				var obj = JSON.parse(data);
				$('#result').append(data);			
			}
		});
	});
	
	//형태소 분석 - text
	$('#morpBtn').click(function(){
		$.ajax({
			url : '${pageContext.request.contextPath }/morp/reqMorp.do?type=text&data=' + $('#morpText').val(),
			success : function(data){
				//var obj = JSON.parse(data);
				alert(data)
				$('#result').text(data);			
			}
		});
	});
	//형태소 분석 - url
	$('#morpUrlBtn').click(function(){
		$.ajax({
			url : '${pageContext.request.contextPath }/morp/reqMorp.do?type=news&data=' + $('#morpUrl').val(),
			success : function(data){
				//var obj = JSON.parse(data);
				$('#result').text(data);			
			}
		});
	});
	
	//뉴스로 주가예측
	$('#analysisBtn').click(function(){
		var param = "?url=" + $('#anaUrl').val() + "&comName=" + $('#anaComName').val() + "&newsCode=" + $('#anaCategory').val() 
					+ "&function=" + $('#anaFunction').val();
		$.ajax({
			url : '${pageContext.request.contextPath }/analysis/analyze.do' + param,
			success : function(data){
				$('#result').append(data);			
			}
		});
	});
});
</script>
</head>
<body>
	<table border="1">
		<tr>
			<td>
				KOSPI 상장 회사 등록하기
				<form action="${pageContext.request.contextPath }/stock/insertCompany.do" method="get" name="stockform">
					<input type="text" name="comName" placeholder="기업명" class="stock">
					<input type="text" name="comCode" placeholder="기업코드" class="stock">
					<input type="text" name="type" value="KOSPI" class="stock">
					<button id="stock">등록</button>
				</form>
			</td>
			<td>
				TFIDF사전 불러오기<br/>
				<input type="text" name="from" id="tfidfFrom">
				<input type="text" name="to" id="tfidfTo">
				<button id="tfidf">호출</button>
				
			</td>
			<td>
				회사 리스트<br/>
				<select name="comName">
					<c:forEach items="${comList }" var="comName">
						<option>${comName }
					</c:forEach>
				</select>
			</td>
		</tr>
		<tr>
			<td>
				형태소 분석<br/>
				<textarea name="text" rows=20 cols=80 id="morpText"></textarea>
				<button id="morpBtn">입력</button>
				뉴스 분석
				<input name="url" type="text" id="morpUrl">
				<button id="morpUrlBtn">입력</button>
			</td>
			<td>
				<form action="${pageContext.request.contextPath }/analysis/trainAnalyze.do">
					예측<br/>
					<input type="text" name="comName" placeholder="회사명">
					<input type="text" name="newsCode" value="economic" readonly="readonly">
					<select name="function">
						<option value="opi1">감정분석1
						<option value="opi2">감정분석2
						<option value="pro1">확률분석1
						<option value="pro2">확률분석2
						<option value="fit1">필터분석1
						<option value="fit2">필터분석2
						<option value="meg1">통합분석1
						<option value="meg2">통합분석2
					</select>
					<br/>
					<input type="date" name="from">
					<input type="date" name="to">
					<input type="submit" value="서브밋">
				</form>
			</td>
			<td>
					뉴스 실시간 예측<br/>
					<input type="text" name="comName" placeholder="회사명" id="anaComName">
					<input type="text" name="newsCode" value="economic" readonly="readonly" id="anaCategory">
					<select name="function" id="anaFunction">
						<option value="opi1">감정분석1
						<option value="opi2">감정분석2
						<option value="pro1">확률분석1
						<option value="pro2">확률분석2
						<option value="fit1">필터분석1
						<option value="fit2">필터분석2
						<option value="meg1">통합분석1
						<option value="meg2">통합분석2
					</select>
					<br/>
					<input type="text" name="url" id="anaUrl">
					<button id="analysisBtn">입력</button>
				</form>
			</td>
		</tr>
		<tr>
			<td colspan="3">
				<div id="result">
					내용
				</div>
			</td>
		</tr>
		<tr>
			
		</tr>
	</table>
	<%-- 
	뉴스 크롤링<br/>
	<form action="${pageContext.request.contextPath }/crawler/crawler.do" method="get">
		<select name="category">
			<option>사회
			<option>정치
			<option>경제
			<option>국제
			<option>문화
			<option>연예
			<option>IT
		</select>
		<input type="date" name="from">
		<input type="date" name="to">
		<input type="submit" value="입력">
	</form>
	 --%>
</body>
</html>