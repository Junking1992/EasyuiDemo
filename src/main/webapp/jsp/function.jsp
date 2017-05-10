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
	<div class="easyui-layout" data-options="fit:true">
		<div data-options="region:'center',border:false">
			<!-- 功能导航区 -->
			<div class="easyui-tabs"
				data-options="tabPosition:'left',fit:true,border:false,headerWidth:150">
				<!-- 功能项 -->
				<div title="茅台酒库项目" style="padding: 10px">
					<p style="color:blue; font-weight:bold;">上传</p>
					<ul>
					   <li>
							<a href="javascript:addTab('tab','酒库资料上传','jsp/jkjj001.jsp');" style="text-decoration:none;color:black;">酒库资料上传1</a>
					   </li>
					</ul>
				</div>
			</div>
		</div>
		<div data-options="region:'east',border:false" style="width: 300px">
		</div>
	</div>
</body>
</html>