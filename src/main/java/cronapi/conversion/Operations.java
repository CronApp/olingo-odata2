package cronapi.conversion;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import cronapi.CronapiMetaData;
import cronapi.Utils;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-03-29
 *
 */
@CronapiMetaData(category = CategoryType.CONVERSION, categoryTags = { "Conversão", "Convert" })
public class Operations {

	@CronapiMetaData(type = "function", name = "{{base64ToText}}", nameTags = {
			"base64ToString" }, description = "{{functionConvertBase64ToText}}", params = {
					"{{contentInBase64}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var base64ToString(Var base64Var) throws Exception {
		Var value = new Var();
		String base64 = base64Var.getObjectAsString();
		if (base64.length() > 0) {
			value = new Var(Base64.getDecoder().decode(base64));
		}
		return value;
	}

	@CronapiMetaData(type = "function", name = "{{textToTextBinary}}", nameTags = {
			"asciiToBinary" }, description = "{{functionToConvertTextInTextBinary}}", params = {
					"{{contentInAscii}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var asciiToBinary(Var asciiVar) throws Exception {
		byte[] bytes = asciiVar.getObjectAsString().getBytes();
		StringBuilder binary = new StringBuilder();
		for (byte b : bytes) {
			int val = b;
			for (int i = 0; i < 8; i++) {
				binary.append((val & 128) == 0 ? 0 : 1);
				val <<= 1;
			}
		}
		return new Var(binary.toString());
	}

	@CronapiMetaData(type = "function", name = "{{textBinaryToText}}", nameTags = {
			"binaryToAscii" }, description = "{{functionToConvertTextBinaryToText}}", params = {
					"{{contentInTextBinary}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var binaryToAscii(Var binaryVar) throws Exception {
		StringBuilder sb = new StringBuilder();
		char[] chars = binaryVar.getObjectAsString().replaceAll("\\s", "").toCharArray();
		for (int j = 0; j < chars.length; j += 8) {
			int idx = 0;
			int sum = 0;
			for (int i = 7; i >= 0; i--) {
				if (chars[i + j] == '1') {
					sum += 1 << idx;
				}
				idx++;
			}
			sb.append(Character.toChars(sum));
		}
		return new Var(sb.toString());
	}

	@CronapiMetaData(type = "function", name = "{{convertToBytes}}", nameTags = {
			"toBytes" }, description = "{{functionToConvertTextBinaryToText}}", params = {
					"{{contentInTextBinary}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var toBytes(Var var) throws Exception {
		return new Var(var.getObjectAsString().getBytes());
	}

	@CronapiMetaData(type = "function", name = "{{convertToAscii}}", nameTags = { "chrToAscii",
			"convertToAscii" }, description = "{{functionToConvertToAscii}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.DOUBLE)
	public static final Var chrToAscii(Var value) throws Exception {
		Var ascii = new Var(null);
		if (value.getObjectAsString() != null && value.getObjectAsString().isEmpty()) {
			ascii = new Var(Long.valueOf(value.getObjectAsString().charAt(0)));
		}
		return ascii;
	}

	@CronapiMetaData(type = "function", name = "{{convertHexadecimalToInt}}", nameTags = { "hexToInt",
			"hexadecimalToInteger" }, description = "{{functionToConvertHexadecimalToInt}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.LONG)
	public static final Var hexToInt(Var value) {
		return new Var(Long.parseLong(value.getObjectAsString(), 16));
	}

	@CronapiMetaData(type = "function", name = "{{convertArrayToList}}", nameTags = {
			"arrayToList" }, description = "{{functionToConvertArrayToList}}", params = {
					"{{content}}" }, paramsType = { ObjectType.LIST }, returnType = ObjectType.LIST)
	public static final Var arrayToList(Var arrayVar) throws Exception {
		List<?> t = Arrays.asList(arrayVar.getObject());
		return new Var(t);
	}

	@CronapiMetaData(type = "function", name = "{{convertBase64ToBinary}}", nameTags = {
			"base64ToBinary" }, description = "{{functionToConvertBase64ToBinary}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var base64ToBinary(Var base64) throws Exception {
		Var binary = new Var(null);
		byte[] temp = Utils.getFromBase64(base64.getObjectAsString());
		if (temp != null)
			binary = new Var(temp);
		return binary;
	}

	@CronapiMetaData(type = "function", name = "{{convertStringToJs}}", nameTags = {
			"stringToJs" }, description = "{{functionToConvertStringToJs}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.STRING)
	public static final Var stringToJs(Var val) throws Exception {
		return new Var(Utils.stringToJs(val.getObjectAsString()));
	}

	@CronapiMetaData(type = "function", name = "{{convertStringToDate}}", nameTags = {
			"stringToDate" }, description = "{{functionToConvertStringToDate}}", params = {
					"{{content}}", "{{mask}}" }, paramsType = { ObjectType.STRING,
							ObjectType.STRING }, returnType = ObjectType.DATETIME)
	public static final Var stringToDate(Var val, Var mask) throws Exception {
		String defaultMask = "dd/MM/yyyy";
		String maskToUse = (mask.getObjectAsString() != null && !mask.getObjectAsString().isEmpty())
				? mask.getObjectAsString() : defaultMask;
		SimpleDateFormat formato = new SimpleDateFormat(maskToUse);
		Date data = formato.parse(val.getObjectAsString());
		return new Var(data);
	}

	@CronapiMetaData(type = "function", name = "{{convertIntToHex}}", nameTags = {
			"intToHex" }, description = "{{functionToConvertIntToHex}}", params = {
					"{{content}}", "{{minSize}}" }, paramsType = { ObjectType.LONG,
							ObjectType.LONG }, returnType = ObjectType.STRING)
	public static final Var intToHex(Var value, Var minSize) throws Exception {
		String hex = Long.toHexString(value.getObjectAsInt());
		while (hex.length() < minSize.getObjectAsInt())
			hex = "0" + hex;
		return new Var(hex);
	}

	@CronapiMetaData(type = "function", name = "{{convertToLong}}", nameTags = {
			"toLong" }, description = "{{functionToConvertToLong}}", params = {
					"{{content}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.LONG)
	public static final Var toLong(Var value) throws Exception {
		return new Var(value.getObjectAsLong());
	}
	
	@CronapiMetaData(type = "function", name = "{{convertToString}}", nameTags = {
			"toString" }, description = "{{functionToConvertToString}}", params = {
					"{{content}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.STRING)
	public static final Var toString(Var value) throws Exception {
    return new Var(value.getObjectAsString());
  }
  
  @CronapiMetaData(type = "function", name = "{{convertToDouble}}", nameTags = {
			"toDouble" }, description = "{{functionToConvertToDouble}}", params = {
					"{{content}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.DOUBLE)
	public final Var toDouble(Var value) throws Exception {
    return new Var(value.getObjectAsDouble());
  }
  
  @CronapiMetaData(type = "function", name = "{{toLogic}}", nameTags = {
			"toBoolean" }, description = "{{functionConvertToLogic}}", params = {
					"{{content}}" }, paramsType = { ObjectType.STRING }, returnType = ObjectType.BOOLEAN)
	public static final Var toBoolean(Var var) throws Exception {
		return new Var(Utils.stringToBoolean(var.getObjectAsString()));
	}
}