<#ftl output_format="HTML">
<p>
	<a href="${url}">${title}</a> 里有新的评论
	（<#if parentFloor != 0>对${parentFloor}楼的回复，</#if>#${floor}）
</p>
<blockquote style="
	padding: 4px 0 4px 10px;
	margin-left: 0;
	border-left: solid 4px #ccc;
">
	${preview}
</blockquote>
