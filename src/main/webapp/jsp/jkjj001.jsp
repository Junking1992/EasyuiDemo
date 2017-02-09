<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="utf-8">
<title>EasyUI</title>
<link rel="stylesheet" type="text/css" href="../jquery-easyui-v1.5/themes/metro/easyui.css">
<link rel="stylesheet" type="text/css" href="../jquery-easyui-v1.5/themes/icon.css">
<link rel="stylesheet" type="text/css" href="../jquery-easyui-v1.5/demo/demo.css">
<script src="../jquery-easyui-v1.5/jquery.min.js"></script>
<script src="../jquery-easyui-v1.5/jquery.easyui.min.js"></script>
</head>
<body>
	<div id="cc" class="easyui-layout" data-options="fit:true">
		<div data-options="region:'west',border:false" style="padding: 15px;width:600px;">
			导入坛动态库存：<input id="uploadA" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadA');" class="easyui-linkbutton" style="width: 70px">上传</a> 
			<img id='msg_uploadA' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadA" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
			导入坛基础资料：<input id="uploadB" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadB');" class="easyui-linkbutton" style="width: 70px">上传</a> 
			<img id='msg_uploadB' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadB" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
			导入库功能分区：<input id="uploadC" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadC');" class="easyui-linkbutton" style="width: 70px">上传</a> 
			<img id='msg_uploadC' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadC" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			核对：<input id="uploadD" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadD');" class="easyui-linkbutton" style="width: 70px">上传</a> 
			<img id='msg_uploadD' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadD" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
		</div>
		<div id="content" data-options="region:'center',border:false" style="padding: 15px;">
		</div>
	</div>
</body>
</html>
