<p>
	在<a href="${url}">${title}</a>里有新的评论
</p>
<#if parentFloor == 0>
	<p>该评论位于第${floor}楼</p>
<#else>
	<p>该评论在${parentFloor}楼的回复里，#${floor}</p>
</#if>
<blockquote>${preview}</blockquote>
