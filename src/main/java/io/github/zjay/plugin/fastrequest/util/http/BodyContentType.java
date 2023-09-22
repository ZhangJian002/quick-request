package io.github.zjay.plugin.fastrequest.util.http;


import quickRequest.icons.PluginIcons;

import javax.swing.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * body：Content-Type类型枚举
 *
 * @author zjay
 */
public enum BodyContentType {

	Text(1, "text/plain", PluginIcons.ICON_TEXT),

	JavaScript(2, "application/javascript", PluginIcons.ICON_JS),

	JSON(3, "application/json", PluginIcons.ICON_JSON),

	HTML(4, "text/html", PluginIcons.ICON_HTML),

	XML(5, "application/xml", PluginIcons.ICON_XML),
	;


	private final Integer code;
	private final String value;

	private final Icon icon;

	BodyContentType(Integer code, String value, Icon icon) {
		this.code = code;
		this.value = value;
		this.icon = icon;
	}

	/**
	 * 获取value值
	 *
	 * @return value值
	 * @since 5.2.6
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 获取code值
	 *
	 * @return code值
	 * @since 5.2.6
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * 获取icon
	 *
	 * @return icon值
	 * @since 5.2.6
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * 获取name值
	 *
	 * @return name值
	 * @since 5.2.6
	 */
	public String getName() {
		return name();
	}

	@Override
	public String toString() {
		return getValue();
	}

	/**
	 * 输出Content-Type字符串，附带编码信息
	 *
	 * @param charset 编码
	 * @return Content-Type字符串
	 */
	public String toString(Charset charset) {
		return build(this.value, charset);
	}


	/**
	 * 输出Content-Type字符串，附带编码信息
	 *
	 * @param contentType Content-Type类型
	 * @param charset     编码
	 * @return Content-Type字符串
	 * @since 4.5.4
	 */
	public static String build(String contentType, Charset charset) {
		return String.format("{};charset={}", contentType, charset.name());
	}

	/**
	 * 输出Content-Type字符串:编码UTF-8
	 *
	 * @param contentType Content-Type类型
	 * @return Content-Type字符串
	 * @since 4.5.4
	 */
	public static String buildUTF_8(String contentType) {
		return build(contentType, StandardCharsets.UTF_8);
	}

}
