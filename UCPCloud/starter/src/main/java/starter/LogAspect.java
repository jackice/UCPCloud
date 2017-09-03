package starter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.sort.SortBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import starter.rest.MultipartParser;
import starter.service.LogService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class LogAspect {
	Logger logger = LoggerFactory.getLogger(LogAspect.class);

	private ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal();

	public Map<String, Object> getThreadLocal() {
		return this.threadLocal.get();
	}

	public void setThreadLocal(Map<String, Object> map) {
		this.threadLocal.set(map);
	}

	public Object getThreadLocal(String key) {
		return getThreadLocal().get(key);
	}

	private final String USER_NAME = "USER_NAME";

	private final String REQUEST_IPADDRESS = "REQUEST_IPADDRESS";
	private final String REQUEST_URL = "REQUEST_URL";
	private final String REQUEST_METHOD = "REQUEST_METHOD";
	private final String REQUEST_PARAMS = "REQUEST_PARAMS";
	private final String REQUEST_HEADER = "REQUEST_HEADER";

	// private final String CLASS_NAME = "CLASS_NAME";
	// private final String METHOD_NAME = "METHOD_NAME";

	private final String RESPONSE_RESULT = "RESPONSE_RESULT";

	private final String EXCEPTION_MSG = "EXCEPTION_MSG";
	private final String EXCEPTION_STATUSCODE = "EXCEPTION_STATUSCODE";
	private final String EXCEPTION_STACKTRACE = "EXCEPTION_STACKTRACE";

	private final String START_TIME = "START_TIME";
	private final String MSG_NONE = "";
	private final String HAS_ERROR = "HAS_ERROR";

	private final String EXECUTION = "execution(public * starter.rest.*.*(..)) "
			//20160608 添加全流程系统日志 by yangbin
			+ "&& execution(public * starter.cfsrest.*.*(..)) "
			+ "&& !execution(public * starter.rest.ErrorHandler.*(..)) " + // ErrorHandler里处理异常，不记录日志
			"&& !execution(public * starter.rest.Initialization.*(..))" + // 系统数据初始化，不记录日志
			"&& !execution(public * starter.rest.Monitor.*(..))" + // 系统监控，不记录日志
			// "&& !execution(public * starter.rest.Documents.*(..))" +
			// //系统管理，不记录日志
			"&& !execution(public * starter.rest.Logs.*(..))"; // Logs里查询日志方法，不记录日志

	private final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	private final String DECIMAL_FORMAT = ",###.###";

	@Autowired
	private RequestContext context;

	@Autowired
	private LogService logService;

	@Pointcut(EXECUTION)
	public void recordLog() {
	}

	@Around("recordLog()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		try {
			doBefore(pjp);

			Object result = pjp.proceed();

			// 记录返回信息,存入本地线程变量
			if (result != null) {
				Map<String, Object> threadLocalMap = getThreadLocal();
				threadLocalMap.put(RESPONSE_RESULT, result);
				setThreadLocal(threadLocalMap);
			}

			// 返回结果给业务逻辑处理
			return result;

		} catch (Throwable ex) {
			// 获取本地线程变量
			Map<String, Object> threadLocalMap = getThreadLocal();

			// 记录异常信息
			threadLocalMap.put(EXCEPTION_MSG, ex + "");

			// 记录异常信息中http状态码
			if (ex instanceof uContentException) {
				threadLocalMap.put(EXCEPTION_STATUSCODE, ((uContentException) ex).getStatusCode() + "");
			}
		
			// 记录异常栈信息
			StringBuffer ex_sb = new StringBuffer();
			for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
				ex_sb.append(stackTraceElement.toString() + "\r\n");
			}
			threadLocalMap.put(EXCEPTION_STACKTRACE, ex_sb.toString());

			// 存入本地线程变量
			setThreadLocal(threadLocalMap);
			
			// 抛出异常给业务逻辑处理
			throw ex;
		}
	}

	public void doBefore(JoinPoint joinpoint) throws Exception {
		try {
			Map<String, Object> threadLocalMap = new HashMap<>();

			// 从joinpoint获取相关信息
			// String className =
			// joinpoint.getSignature().getDeclaringTypeName();
			// threadLocalMap.put(CLASS_NAME, className);
			//
			// String methodName = joinpoint.getSignature().getName();
			// threadLocalMap.put(METHOD_NAME, methodName);

			// 获取请求参数信息
			Object[] args = joinpoint.getArgs();
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					if (args[i] instanceof SortBuilder[]) { // 解析SortBuilder参数信息，还原为字符串类型的参数
						SortBuilder[] sorts = (SortBuilder[]) args[i];
						if (sorts != null && sorts.length != 0) {// 非默认值时进行处理
							String sortString = "[";
							for (SortBuilder sort : sorts) {
								sortString += sort.toString() + ",";
							}
							sortString = sortString.substring(0, sortString.length() - 1) + "]";
							sortString = sortString.replaceAll("[\\t\\n\\r]", "");// 去掉回车换行
							args[i] = sortString;
						} else {// 默认值的时候不处理
							args[i] = "[]";
						}
					} else if (args[i] instanceof StandardMultipartHttpServletRequest) { // 解析上传的文件流的参数信息，转换为字符串类型的参数
						StandardMultipartHttpServletRequest multipartHttpServletRequest = (StandardMultipartHttpServletRequest) args[i];
						MultipartParser parser = new MultipartParser(multipartHttpServletRequest).invoke();
						List<MultipartFile> files = parser.getFiles();
						if (!files.isEmpty()) {
							String fileInfoString = "[";
							for (MultipartFile multipartFile : files) {
								// byte[] bytes = multipartFile.getBytes();
								// InputStream inputStream =
								// multipartFile.getInputStream();
								String name = multipartFile.getName();
								long size = multipartFile.getSize();
								String contentType = multipartFile.getContentType();
								String originalFilename = multipartFile.getOriginalFilename();
								fileInfoString += "{name:" + name + ",size:" + size + ",contentType:" + contentType
										+ ",originalFilename:" + originalFilename + "},";
							}
							fileInfoString = fileInfoString.substring(0, fileInfoString.length() - 1) + "]";
							args[i] = fileInfoString;
						} else {
							args[i] = "[]";
						}
					}
				}
				String requestParams = Arrays.toString(args);
				threadLocalMap.put(REQUEST_PARAMS, requestParams);
			}

			// 从request获取相关信息
			HttpServletRequest request = context.getRequest();

			String userName = request.getUserPrincipal().getName();
			threadLocalMap.put(USER_NAME, userName);

			String ipAddress = LogAspect.getIpAddress(request);
			threadLocalMap.put(REQUEST_IPADDRESS, ipAddress);

			String url = request.getRequestURL().toString();
			threadLocalMap.put(REQUEST_URL, url);

			String httpMethod = request.getMethod();
			threadLocalMap.put(REQUEST_METHOD, httpMethod);

			// 获取header信息
			Enumeration<String> headerNames = request.getHeaderNames();
			List<String> headerList = new ArrayList<>();
			for (; headerNames.hasMoreElements();) {
				String thisName = headerNames.nextElement();
				String thisValue = request.getHeader(thisName);
				headerList.add("{" + thisName + ":" + thisValue + "}");
			}
			Object[] headerObjects = headerList.toArray();
			String headerInfo = Arrays.toString(headerObjects);
			threadLocalMap.put(REQUEST_HEADER, headerInfo);

			// 记录开始时间
			long startTimeMillis = System.currentTimeMillis();
			threadLocalMap.put(START_TIME, startTimeMillis);

			// 存入本地线程变量
			setThreadLocal(threadLocalMap);
		} catch (Throwable ex) {
			logger.error(String.format("doBefore failed: %s", ex));
		}
	}

	@After("recordLog()")
	public void doAfter() throws uContentException {
		try {
			// 记录结束时间
			long endTime = System.currentTimeMillis();
			String endTime_format = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(endTime);// 格式化,为了展示使用

			// 计算耗时
			long startTime = (long) getThreadLocal(START_TIME);
			String startTime_format = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(startTime);// 格式化,为了展示使用
			long time_consuming = endTime - startTime;
			String time_consuming_format = new DecimalFormat(DECIMAL_FORMAT).format(time_consuming) + "ms";// 格式化,为了展示使用

			// 从本地线程变量获取信息
			// String className = (String) getThreadLocal(CLASS_NAME);
			// String methodName = (String) getThreadLocal(METHOD_NAME);

			String userName = (String) getThreadLocal(USER_NAME);

			String request_ipAddress = (String) getThreadLocal(REQUEST_IPADDRESS);
			String request_url = (String) getThreadLocal(REQUEST_URL);
			String request_method = (String) getThreadLocal(REQUEST_METHOD);
			String request_params = MSG_NONE;
			if (getThreadLocal().containsKey(REQUEST_PARAMS)) {
				request_params = (String) getThreadLocal(REQUEST_PARAMS);
			}
			String request_header = (String) getThreadLocal(REQUEST_HEADER);

			// 从response获取相关信息
			HttpServletResponse response = context.getResponse();
			int response_statusCode = response.getStatus();
			Collection<String> headerNames = response.getHeaderNames();
			List<String> headerList = new ArrayList<>();
			for (String headerName : headerNames) {
				String header = response.getHeader(headerName);
				headerList.add("{" + headerName + ":" + header + "}");
			}
			Object[] headerObjects = headerList.toArray();
			String response_header = Arrays.toString(headerObjects);
			Object result = getThreadLocal(RESPONSE_RESULT);
			String response_result = MSG_NONE;
			if (result != null) {
				response_result = result + "";
			}

			String ex_msg = MSG_NONE;
			boolean hasError = false;
			if (getThreadLocal().containsKey(EXCEPTION_MSG)) {
				ex_msg = (String) getThreadLocal(EXCEPTION_MSG);
				hasError = true;
			}
			String ex_statusCode = MSG_NONE;
			if (getThreadLocal().containsKey(EXCEPTION_STATUSCODE)) {
				ex_statusCode = (String) getThreadLocal(EXCEPTION_STATUSCODE);
			}
			String ex_stackTrace = MSG_NONE;
			if (getThreadLocal().containsKey(EXCEPTION_STACKTRACE)) {
				ex_stackTrace = (String) getThreadLocal(EXCEPTION_STACKTRACE);
			}

			// 构建JSON格式对象
			XContentBuilder builder;

			builder = XContentFactory.jsonBuilder();
			builder.startObject().field("userName", userName)

			.startObject("timeInfo").field("start", startTime).field("start_format", startTime_format)
					.field("end", endTime).field("end_format", endTime_format).field("consume", time_consuming)
					.field("consume_format", time_consuming_format)
					.endObject()
					// .field("actionInfo.className", className)
					// .field("actionInfo.methodName", methodName)
					.startObject("requestInfo").field("ipAddress", request_ipAddress).field("url", request_url)
					.field("method", request_method).field("params", request_params);
			if (!"GET".equals(request_method)) {
				builder.field("header", request_header);
			}
			builder.endObject().startObject("responseInfo").field("statusCode", response_statusCode);
			if (!"GET".equals(request_method)) {
				builder.field("header", response_header).field("result", response_result);
			}
			builder.endObject()

			.field("hasError", hasError).startObject("exceptionInfo").field("msg", ex_msg)
					.field("statusCode", ex_statusCode).field("stackTrace", ex_stackTrace).endObject()
					.field("logDate", new DateTime().toLocalDateTime()).endObject();

			logService.createLog(builder);

		} catch (Throwable e) {
			logger.error(String.format("logService.createLog failed: %s", e));
		}

	}

	// 获取远程IP的真实地址
	private static String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}

}