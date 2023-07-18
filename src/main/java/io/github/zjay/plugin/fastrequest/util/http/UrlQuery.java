package io.github.zjay.plugin.fastrequest.util.http;

import io.github.zjay.plugin.fastrequest.util.TableMap;
import io.github.zjay.plugin.fastrequest.util.URLDecoder;
import io.github.zjay.plugin.fastrequest.util.URLEncoder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * URL中查询字符串部分的封装，类似于：
 * <pre>
 *   key1=v1&amp;key2=&amp;key3=v3
 * </pre>
 *
 * @author looly
 * @since 5.3.1
 */
public class UrlQuery {

	private final TableMap<CharSequence, CharSequence> query;

	/**
	 * 构建UrlQuery
	 *
	 * @param queryMap 初始化的查询键值对
	 * @return UrlQuery
	 */
	public static UrlQuery of(Map<? extends CharSequence, ?> queryMap) {
		return new UrlQuery(queryMap);
	}

	/**
	 * 构建UrlQuery
	 *
	 * @param queryStr 初始化的查询字符串
	 * @param charset  decode用的编码，null表示不做decode
	 * @return UrlQuery
	 */
	public static UrlQuery of(String queryStr, Charset charset) {
		return of(queryStr, charset, true);
	}

	/**
	 * 构建UrlQuery
	 *
	 * @param queryStr       初始化的查询字符串
	 * @param charset        decode用的编码，null表示不做decode
	 * @param autoRemovePath 是否自动去除path部分，{@code true}则自动去除第一个?前的内容
	 * @return UrlQuery
	 * @since 5.5.8
	 */
	public static UrlQuery of(String queryStr, Charset charset, boolean autoRemovePath) {
		final UrlQuery urlQuery = new UrlQuery();
		urlQuery.parse(queryStr, charset, autoRemovePath);
		return urlQuery;
	}

	/**
	 * 构造
	 */
	public UrlQuery() {
		this(null);
	}

	/**
	 * 构造
	 *
	 * @param queryMap 初始化的查询键值对
	 */
	public UrlQuery(Map<? extends CharSequence, ?> queryMap) {
		if (MapUtils.isNotEmpty(queryMap)) {
			query = new TableMap<>(queryMap.size());
			addAll(queryMap);
		} else {
			query = new TableMap<>(16);
		}
	}

	/**
	 * 增加键值对
	 *
	 * @param key   键
	 * @param value 值，集合和数组转换为逗号分隔形式
	 * @return this
	 */
	public UrlQuery add(CharSequence key, Object value) {
		this.query.put(key, toStr(value));
		return this;
	}

	/**
	 * 批量增加键值对
	 *
	 * @param queryMap query中的键值对
	 * @return this
	 */
	public UrlQuery addAll(Map<? extends CharSequence, ?> queryMap) {
		if (MapUtils.isNotEmpty(queryMap)) {
			queryMap.forEach(this::add);
		}
		return this;
	}

	/**
	 * 解析URL中的查询字符串
	 *
	 * @param queryStr 查询字符串，类似于key1=v1&amp;key2=&amp;key3=v3
	 * @param charset  decode编码，null表示不做decode
	 * @return this
	 */
	public UrlQuery parse(String queryStr, Charset charset) {
		return parse(queryStr, charset, true);
	}

	/**
	 * 解析URL中的查询字符串
	 *
	 * @param queryStr       查询字符串，类似于key1=v1&amp;key2=&amp;key3=v3
	 * @param charset        decode编码，null表示不做decode
	 * @param autoRemovePath 是否自动去除path部分，{@code true}则自动去除第一个?前的内容
	 * @return this
	 * @since 5.5.8
	 */
	public UrlQuery parse(String queryStr, Charset charset, boolean autoRemovePath) {
		if (StringUtils.isBlank(queryStr)) {
			return this;
		}

		if (autoRemovePath) {
			// 去掉Path部分
			int pathEndPos = queryStr.indexOf('?');
			if (pathEndPos > -1) {
				queryStr = HttpUtil.subSuf(queryStr, pathEndPos + 1);
				if (StringUtils.isBlank(queryStr)) {
					return this;
				}
			}
		}

		final int len = queryStr.length();
		String name = null;
		int pos = 0; // 未处理字符开始位置
		int i; // 未处理字符结束位置
		char c; // 当前字符
		for (i = 0; i < len; i++) {
			c = queryStr.charAt(i);
			switch (c) {
				case '='://键和值的分界符
					if (null == name) {
						// name可以是""
						name = queryStr.substring(pos, i);
						// 开始位置从分节符后开始
						pos = i + 1;
					}
					// 当=不作为分界符时，按照普通字符对待
					break;
				case '&'://键值对之间的分界符
					addParam(name, queryStr.substring(pos, i), charset);
					name = null;
					if (i + 4 < len && "amp;".equals(queryStr.substring(i + 1, i + 5))) {
						// issue#850@Github，"&amp;"转义为"&"
						i += 4;
					}
					// 开始位置从分节符后开始
					pos = i + 1;
					break;
			}
		}

		if(i - pos == len){
			// 没有任何参数符号
			if(queryStr.startsWith("http") || queryStr.contains("/")){
				// 可能为url路径，忽略之
				return this;
			}
		}

		// 处理结尾
		addParam(name, queryStr.substring(pos, i), charset);

		return this;
	}

	/**
	 * 获取查询值
	 *
	 * @param key 键
	 * @return 值
	 */
	public CharSequence get(CharSequence key) {
		if (MapUtils.isEmpty(this.query)) {
			return null;
		}
		return this.query.get(key);
	}

	/**
	 * 构建URL查询字符串，即将key-value键值对转换为key1=v1&amp;key2=&amp;key3=v3形式
	 *
	 * @param charset encode编码，null表示不做encode编码
	 * @return URL查询字符串
	 */
	public String build(Charset charset) {
		if (MapUtils.isEmpty(this.query)) {
			return StringUtils.EMPTY;
		}

		final StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		CharSequence key;
		CharSequence value;
		for (Map.Entry<CharSequence, CharSequence> entry : this.query) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append("&");
			}
			key = entry.getKey();
			if (null != key) {
				sb.append(URLEncoder.ALL.encode(key.toString(), charset));
				value = entry.getValue();
				if (null != value) {
					sb.append("=").append(URLEncoder.ALL.encode(value.toString(), charset));
				}
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return build(StandardCharsets.UTF_8);
	}

	/**
	 * 对象转换为字符串，用于URL的Query中
	 *
	 * @param value 值
	 * @return 字符串
	 */
	private static String toStr(Object value) {
		String result;
		if (value instanceof Iterable) {
			result = StringUtils.join((Iterable<?>) value, ",");
		} else if (value instanceof Iterator) {
			result = StringUtils.join((Iterator<?>) value, ",");
		} else {
			result = value.toString();
		}
		return result;
	}

	/**
	 * 将键值对加入到值为List类型的Map中,，情况如下：
	 * <pre>
	 *     1、key和value都不为null，类似于 "a=1"或者"=1"，直接put
	 *     2、key不为null，value为null，类似于 "a="，值传""
	 *     3、key为null，value不为null，类似于 "1"
	 *     4、key和value都为null，忽略之，比如&&
	 * </pre>
	 *
	 * @param key     key，为null则value作为key
	 * @param value   value，为null且key不为null时传入""
	 * @param charset 编码
	 */
	private void addParam(String key, String value, Charset charset) {
		if (null != key) {
			final String actualKey = URLDecoder.decode(key, charset);
			String decode = URLDecoder.decode(value, charset);
			this.query.put(actualKey, decode == null ? "" : decode);
		} else if (null != value) {
			// name为空，value作为name，value赋值null
			this.query.put(URLDecoder.decode(value, charset), null);
		}
	}
}
