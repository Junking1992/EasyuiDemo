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
			坛动态库存导入：<input id="uploadA" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadA');" class="easyui-linkbutton" style="width: 70px">上传</a>
			<img id='msg_uploadA' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadA" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
			坛基础资料导入：<input id="uploadB" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadB');" class="easyui-linkbutton" style="width: 70px">上传</a> 
			<img id='msg_uploadB' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadB" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
			库功能分区导入：<input id="uploadC" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadC');" class="easyui-linkbutton" style="width: 70px">上传</a> 
			<img id='msg_uploadC' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadC" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
			大灌资料导入：&nbsp;&nbsp;&nbsp;<input id="uploadD" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadD');" class="easyui-linkbutton" style="width: 70px">上传</a> 
			<img id='msg_uploadD' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadD" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
			大灌库存导入：&nbsp;&nbsp;&nbsp;<input id="uploadE" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadE');" class="easyui-linkbutton" style="width: 70px">上传</a> 
			<img id='msg_uploadE' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadE" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
			坛号库存导入：&nbsp;&nbsp;&nbsp;<input id="uploadF" class="easyui-filebox" style="width:300px">
			<a id="btn" href="javascript:upLoad('uploadF');" class="easyui-linkbutton" style="width: 70px">上传</a> 
			<img id='msg_uploadF' src='jquery-easyui-v1.5/themes/icons/ok.png' style="display:none;">
			<br/><br/><div id="progressbar_uploadF" class="easyui-progressbar" style="width:396px;display:none;"></div>
			<br/>
			<a id="btn" href="javascript:autoUpLoad('uploadG');" class="easyui-linkbutton" style="width: 70px">自动上传</a>
		</div>
		<div id="content" data-options="region:'center',border:false" style="padding: 15px;">
		</div>
	</div>
</body>
</html>
