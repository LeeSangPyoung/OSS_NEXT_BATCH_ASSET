<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@ page import="nexcore.scheduler.core.VERSION"%>
<%@ include file= "common.jsp" %>

	<tr><td height="1" style="background-color:gray;"></td></tr>
	<tr style="vertical-align:bottom;">
		<td height="50">
			<!-- BOTTOM 영역 시작 -->
			<div class="footer">
				<div class="footer-wrap">
					<div class="version-info">
					Copyright &copy; <%=nexcore.scheduler.core.VERSION.getBuildYear()%> SK Holdings Co., Ltd. All rights reserved.
					(Version : <%=VERSION.getImplementationVersion()%> | Build : <%=VERSION.getBuildTime()%>)
					</div>
				</div>
			</div>					
			<!-- BOTTOM 영역 종료 -->
		</td>
	</tr>			
