package cronapi.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

/**
 * Classe que representa ...
 * 
 * @author Rodrigo Reis
 * @version 1.0
 * @since 2017-03-31
 *
 */
@CronapiMetaData(category = CategoryType.UTIL, categoryTags = { "Util" })
public class Operations {

	/**
	 * Construtor
	 **/
	public Operations() {
	}

	@CronapiMetaData(type = "function", name = "{{copyTextToTransferAreaName}}", nameTags = {
			"copyTextToTransferArea" }, description = "{{copyTextToTransferAreaDescription}}", params = {
					"{{copyTextToTransferAreaParam0}}" }, paramsType = { ObjectType.STRING })
	public static final void copyTextToTransferArea(Var strVar) throws Exception {
		String str = strVar.getObjectAsString();
		java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
		java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(str);
		clipboard.setContents(selection, null);
	}

	@CronapiMetaData(type = "function", name = "{{shellExecuteName}}", nameTags = {
			"shellExecute" }, description = "{{shellExecuteDescription}}", params = { "{{shellExecuteParam0}}",
					"{{shellExecuteParam1}}" }, paramsType = { ObjectType.STRING,
							ObjectType.BOOLEAN }, returnType = ObjectType.STRING)
	public static final Var shellExecute(Var cmdline, Var waitFor) throws Exception {
		Boolean waitForCasted = (Boolean) waitFor.getObject();
		Process p = Runtime.getRuntime().exec(cmdline.getObjectAsString());
		if (waitForCasted) {
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String r = "";
			String line;
			while ((line = input.readLine()) != null) {
				r += (line + "\n");
			}
			input.close();
			return new Var(r);
		}
		return new Var();
	}

	// Retorna um numério aleatório 
	@CronapiMetaData(type = "function", name = "{{randomName}}", nameTags = {
			"random" }, description = "{{randomDescription}}", params = {
					"{{randomParam0}}" }, paramsType = { ObjectType.DOUBLE }, returnType = ObjectType.DOUBLE)
	public static final Var random(Var maxValue) throws Exception {
		return new Var(Math.round(Math.random() * maxValue.getObjectAsDouble()));
	}

	@CronapiMetaData(type = "function", name = "{{compressToZipName}}", nameTags = {
			"compressToZip" }, description = "{{compressToZipDescription}}", params = {
					"{{compressToZipParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var compressToZip(Var value) throws Exception {
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		java.util.zip.DeflaterOutputStream compresser = new java.util.zip.DeflaterOutputStream(output);
		compresser.write((byte[]) value.getObject());
		compresser.finish();
		compresser.close();
		return new Var(output.toByteArray());
	}

	@CronapiMetaData(type = "function", name = "{{decodeZipFromByteName}}", nameTags = {
			"decodeZipFromByte" }, description = "{{decodeZipFromByteDescription}}", params = {
					"{{decodeZipFromByteParam0}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.OBJECT)
	public static final Var decodeZipFromByte(Var value) throws Exception {
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream((byte[]) value.getObject());
		java.util.zip.InflaterInputStream decompresser = new java.util.zip.InflaterInputStream(input);
		byte[] buffer = new byte[1024 * 4];//4KB
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		int len;
		while ((len = decompresser.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		decompresser.close();
		out.close();
		input.close();
		return new Var(out.toByteArray());
	}

	@CronapiMetaData(type = "function", name = "{{sleep}}", nameTags = {
			"sleep" }, description = "{{functionToSleep}}", params = {
					"{{timeSleepInSecond}}" }, paramsType = { ObjectType.LONG }, returnType = ObjectType.VOID)
	public static final void sleep(Var time) throws Exception {
		long sleepTime = (time.getObjectAsLong() * 1000);
		Thread.sleep(sleepTime);
	}

	@CronapiMetaData(type = "function", name = "{{throwException}}", nameTags = {
			"throwException" }, description = "{{functionToThrowException}}", params = {
					"{{exceptionToBeThrow}}" }, paramsType = { ObjectType.OBJECT }, returnType = ObjectType.VOID)
	public static final void throwException(Var exception) throws Exception {
		if (exception.getObject() instanceof Exception)
			throw Exception.class.cast(exception.getObject());
		else if (exception.getObject() instanceof String)
			throw new Exception(exception.getObjectAsString());
	}

	@CronapiMetaData(type = "function", name = "{{callBlockly}}", nameTags = {
			"callBlockly" }, description = "{{functionToCallBlockly}}", params = { "{{classNameWithMethod}}",
					"{{params}}" }, paramsType = { ObjectType.OBJECT,
							ObjectType.OBJECT }, returnType = ObjectType.VOID, arbitraryParams = true)
	public static final Var callBlockly(Var classNameWithMethod, Var... params) throws Exception {
		try {

			String className = classNameWithMethod.getObjectAsString();
			String method = "";
			if (className.indexOf(":") > -1) {
				method = className.substring(className.indexOf(":") + 1);
				className = className.substring(0, className.indexOf(":"));
			}

			Class[] arrayClass = null;
			if (params != null && params.length > 0) {
				arrayClass = new Class[params.length];
				for (int i = 0; i < params.length; i++)
					arrayClass[i] = Var.class;
			}

			Class clazz = Class.forName(className);
			Method methodToCall = null;
			if (method.isEmpty())
				methodToCall = clazz.getDeclaredMethods()[0];
			else
				methodToCall = clazz.getDeclaredMethod(method, arrayClass);

			Object obj = clazz.newInstance();
			Object o = methodToCall.invoke(obj, params);
			return new Var(o);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public static final Var callBlockly(Object classNameWithMethod, Object... params) throws Exception {
		try {
			Var classNameWithMethodVar = null;
			Var[] vars = null;

			if (classNameWithMethod instanceof Var)
				classNameWithMethodVar = (Var) classNameWithMethod;
			else
				classNameWithMethodVar = new Var(classNameWithMethod);

			if (params != null && params.length > 0) {
				vars = new Var[params.length];
				int i = 0;
				for (Object value : params) {
					if (value instanceof Var)
						vars[i++] = (Var) value;
					else
						vars[i++] = new Var(value);
				}
			}
			return callBlockly(classNameWithMethodVar, vars);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}