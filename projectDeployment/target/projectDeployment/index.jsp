<%@ page contentType="text/html;charset=utf-8"%>
<html>
<head>
    <title>代码部署</title>
    <style>
        body{margin: 0 0 0 0;}
        ul li{list-style: none}
    </style>
    <script src="./jquery3.5.1.js"></script>
</head>
<body>
<form method="post" id="tf" action="<%=request.getContextPath()%>/tupload.uf" enctype="multipart/form-data" accept-charset="UTF-8">
    tomcat服务器：<input type="file" id="t" name="uploadFile" readonly="true"/><input type="button" value="上传" onclick="submitForm(this.form)"/>
</form>
<form method="post" id="jf"  action="<%=request.getContextPath()%>/jupload.uf" enctype="multipart/form-data"accept-charset="UTF-8">
   jboss  服务器：&nbsp;&nbsp;<input type="file" id="j" name="uploadFile" readonly="true"/><input type="button" value="上传" onclick="submitForm(this.form)"/>
</form>
<h2>日志查看</h2>
<div>
    <select id="ip">
        <option>139.9.151.109</option>
        <option>172.16.27.169</option>
        <option>172.16.27.170</option>
        <option>172.16.27.171</option>
        <option>172.16.27.172</option>
    </select>
    <ul>
        <li><a href="" onclick="javascript:down(this,'8080/mom-ass');">装配日志</a></li>
        <li><a href="" onclick="javascript:down(this,'8080/mom-part');">零件日志</a></li>
        <li><a href="" onclick="javascript:down(this,'8080/mom-insp');">检验日志</a></li>
        <li><a href="" onclick="javascript:down(this,'8080/mom-plane');">试飞日志</a></li>
        <li><a href="" onclick="javascript:down(this,'8080/mom-wms');">库存后端日志</a></li>
        <li><a href="" onclick="javascript:down(this,'8080/mom-wms-front');">库存前端日志</a></li>
        <li><a href="" onclick="javascript:down(this,'8080/mom_main');">主页日志</a></li>
    </ul>
</div>
<script>
    function down(e,sys) {
        var ip = document.getElementById("ip").value;
        var path ="http://" + ip + ":" + sys + "/run.log";
        e.href = path;
    }
    function submitForm(form){
        var file = $("#t").val();
        if(!file){
            alert("请选择需要上传的文件");
            return;
        }
        form.submit();
    }
</script>
</body>
</html>
