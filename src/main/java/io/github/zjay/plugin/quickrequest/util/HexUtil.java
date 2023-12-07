package io.github.zjay.plugin.quickrequest.util;



/**
 * 十六进制（简写为hex或下标16）在数学中是一种逢16进1的进位制，一般用数字0到9和字母A到F表示（其中:A~F即10~15）。<br>
 * 例如十进制数57，在二进制写作111001，在16进制写作39。<br>
 * 像java,c这样的语言为了区分十六进制和十进制数值,会在十六进制数的前面加上 0x,比如0x20是十进制的32,而不是十进制的20<br>
 * <p>
 *
 * @author Looly
 */
public class HexUtil {

	/**
	 * 用于建立十六进制字符的输出的小写字符数组
	 */
	private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	/**
	 * 用于建立十六进制字符的输出的大写字符数组
	 */
	private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


	/**
	 * 将字节数组转换为十六进制字符串
	 *
	 * @param data        byte[]
	 * @param toLowerCase {@code true} 传换成小写格式 ， {@code false} 传换成大写格式
	 * @return 十六进制String
	 */
	public static String encodeHexStr(byte[] data, boolean toLowerCase) {
		return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
	}

	// ---------------------------------------------------------------------------------------------------- decode


	/**
	 * 将byte值转为16进制并添加到{@link StringBuilder}中
	 *
	 * @param builder     {@link StringBuilder}
	 * @param b           byte
	 * @param toLowerCase 是否使用小写
	 * @since 4.4.1
	 */
	public static void appendHex(StringBuilder builder, byte b, boolean toLowerCase) {
		final char[] toDigits = toLowerCase ? DIGITS_LOWER : DIGITS_UPPER;

		int high = (b & 0xf0) >>> 4;//高位
		int low = b & 0x0f;//低位
		builder.append(toDigits[high]);
		builder.append(toDigits[low]);
	}


	/**
	 * 格式化Hex字符串，结果为每2位加一个空格，类似于：
	 * <pre>
	 *     e8 8c 67 03 80 cb 22 00 95 26 8f
	 * </pre>
	 *
	 * @param hexStr Hex字符串
	 * @return 格式化后的字符串
	 */
	public static String format(String hexStr) {
		final int length = hexStr.length();
		final StringBuilder builder = new StringBuilder(length + length / 2);
		builder.append(hexStr.charAt(0)).append(hexStr.charAt(1));
		for (int i = 2; i < length - 1; i += 2) {
			builder.append(CharPool.SPACE).append(hexStr.charAt(i)).append(hexStr.charAt(i + 1));
		}
		return builder.toString();
	}

	// ---------------------------------------------------------------------------------------- Private method start

	/**
	 * 将字节数组转换为十六进制字符串
	 *
	 * @param data     byte[]
	 * @param toDigits 用于控制输出的char[]
	 * @return 十六进制String
	 */
	private static String encodeHexStr(byte[] data, char[] toDigits) {
		return new String(encodeHex(data, toDigits));
	}

	/**
	 * 将字节数组转换为十六进制字符数组
	 *
	 * @param data     byte[]
	 * @param toDigits 用于控制输出的char[]
	 * @return 十六进制char[]
	 */
	private static char[] encodeHex(byte[] data, char[] toDigits) {
		final int len = data.length;
		final char[] out = new char[len << 1];//len*2
		// two characters from the hex value.
		for (int i = 0, j = 0; i < len; i++) {
			out[j++] = toDigits[(0xF0 & data[i]) >>> 4];// 高位
			out[j++] = toDigits[0x0F & data[i]];// 低位
		}
		return out;
	}

}
