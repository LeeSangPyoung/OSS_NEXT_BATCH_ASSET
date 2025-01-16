<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@ page session="true" %>
<%@ page import="java.util.*"%>
<script type="text/javascript">
    function displayMsg() {
<%
        List<String> mymsg = (List<String>)session.getAttribute("MY_MESSAGE");
        session.removeAttribute("MY_MESSAGE");
        if (mymsg != null) {
            for (String msg : mymsg) {
                out.println("alert(\""+msg.replaceAll("\"", "\\\\\"")+"\");");
            }
        }
%>
    }
</script>