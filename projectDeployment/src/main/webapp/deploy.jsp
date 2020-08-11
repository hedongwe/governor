<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>title</title>
</head>
<body>
//测试
<form id="dfrom"  method="post" action="<%=request.getContextPath()%>/deploy.uf">
    <input type="hidden" name="fn" value="<%=request.getAttribute("message")%>">
    <H1>程序包名称： <%=request.getAttribute("message")%></H1>
    <ul>
        <li><input type="checkbox" checked="true" name="ip" value="139.9.151.109"/>139.9.151.109</li>
        <li><input type="checkbox" checked="true" name="ip" value="172.16.27.169"/>172.16.27.169</li>
        <li><input type="checkbox" checked="true" name="ip" value="172.16.27.170"/>172.16.27.170</li>
        <li><input type="checkbox" checked="true" name="ip" value="172.16.27.171"/>172.16.27.171</li>
        <li><input type="checkbox" checked="true" name="ip" value="172.16.27.172"/>172.16.27.172</li>
        <li><input type="checkbox" checked="true" name="ip" value="192.168.3.31"/>192.168.3.31</li>
        <li><input type="checkbox" checked="true" name="ip" value="192.168.3.32"/>192.168.3.32</li>
    </ul>
    <input type="button" value = "部署" onclick="submitForm(this.form)"/>
</form>
<script>
    function submitForm(form){
        var t = false;
        var ips = form.ip;
        for(var i=0;i<ips.length;i++){
            t = ips[i].checked || t;
        }
        if(!t){
            alert("请选择部署的服务器！");
        }else{
            form.submit();
        }
    }
</script>
</body>
</html>
