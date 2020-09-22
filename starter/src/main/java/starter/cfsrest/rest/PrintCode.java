package starter.cfsrest.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import starter.cfsrest.bean.Constant;
import starter.cfsrest.service.CFSTreeService;

@Controller
@RequestMapping(value = "/cfs", produces = MediaType.APPLICATION_JSON_VALUE)
public class PrintCode {
	@Autowired
	private CFSTreeService cfsTreeService;
	
	/*
	 * 根据页面传过来的printType和BarCode进行拼接JSON字符串
	 * 当PrintType为01/03时，拼接部分加上BarCode。但是BarCode要根据逗号进行分割，分割成数组格式
	 * 当PrintType=04时，进行查询，获取目录，传到页面ajax进行继续处理
	 */
	@RequestMapping(value = "/tree_data/{type}", method = {RequestMethod.GET})
	public void queryTreeData(@PathVariable String type,
			@RequestParam(defaultValue = "0") int start,
			@RequestParam(defaultValue = "10") int limit,
			@RequestParam(defaultValue = "") String funCode,
			@RequestParam(defaultValue = "") String PrintType,
			@RequestParam(defaultValue = "") String BarCode,
			HttpServletResponse response,
			HttpServletRequest request
			){

		if(PrintType.equals("01")||PrintType.equals("03")){
			StringBuilder jsonStr = new StringBuilder();
			if(PrintType.equals("01")){
				jsonStr.append("{\"typeCode\":\"客户号流水号\",");
				jsonStr.append("\"PrintType\":\"01\",");

			}
			if(PrintType.equals("03")){
				jsonStr.append("{\"typeCode\":\"业务号流水号\",");
				jsonStr.append("\"PrintType\":\"03\",");
			}else if(PrintType.equals("04")){
				jsonStr.append("{\"typeCode\":\"default\",");
				jsonStr.append("\"PrintType\":\"04\",");
			}
			String [] print=BarCode.split(",");
			jsonStr.append("\"BarCode\":[");
			for(int i=0;i<print.length;i++){
				if(i!=print.length-1){
					jsonStr.append("\""+print[i]+"\",");
				}else{
					jsonStr.append("\""+print[i]+"\"");
				}

			}
			jsonStr.append("],");


			jsonStr.append("\"last\":\"last\"}");
			System.out.println(jsonStr);

			try{
				response.setCharacterEncoding("utf-8");
				response.setContentType("application/json");
				PrintWriter writer= response.getWriter();
				writer.write(jsonStr.toString());
			}catch(IOException e){
				e.printStackTrace();
			}
		}else if(PrintType.equals("04")){
			XContentBuilder builder = null;
			String treeData = null;
			try {

				builder = cfsTreeService.query(type, start, limit,funCode,Constant.CFS_COMMON_DIGIT_STRING_ZERO);
				treeData = builder.string();

				response.setCharacterEncoding("utf-8");
				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				writer.write(treeData);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}



	}
	
	
	/*
	 *根据ajax传过来的参数，拼接JSON字符串
	 * 
	 */
	@RequestMapping(value = "/scan", method = {RequestMethod.GET})
	public void ScanAndLook(
			@RequestParam(defaultValue = "cfsuser") String username,
			@RequestParam(defaultValue = "cfsuser") String password,
			@RequestParam(defaultValue = "cfs_master") String ucpType,
			HttpServletResponse response,
			HttpServletRequest request
			){

		String str=request.getParameter("value");
		String SystemCode=request.getParameter("SystemCode");
		String FunctionCode=request.getParameter("FunctionCode");
		String CustomerCode=request.getParameter("CustomerCode");
		String BusinessCode=request.getParameter("BusinessCode");
		String BatchCode=request.getParameter("BatchCode");
		String OrgCode=request.getParameter("OrgCode");
		String UserCode=request.getParameter("UserCode");
		String ImgCode=request.getParameter("ImgCode");
		String AuthorizCode=request.getParameter("AuthorizCode");
		String ScanFlag=request.getParameter("ScanFlag");
		String IsBatch=request.getParameter("IsBatch");
		String CheckFlag=request.getParameter("CheckFlag");;
		String FileFlag=request.getParameter("FileFlag");
		String FlowFlag=request.getParameter("FlowFlag");
		String BarCode=request.getParameter("BarCode");
		String PrintType=request.getParameter("PrintType");
		String ActiveType=request.getParameter("ActiveType");


		String [] urlArr=str.split("/");
		StringBuilder urlBuilder=new StringBuilder();
		for(int i=0;i<3;i++){

			urlBuilder.append(urlArr[i]);
			if(i==0){
				urlBuilder.append("//");

			}
		}
		String url=urlBuilder.toString();
	



		//String url="http://130.1.11.200:8080";
		//Map<String, String> mapRequest = AnalysisURL.urlRequest(str);
		//String printType=mapRequest.get("PrintType");

		if("null".equals(SystemCode)){
			SystemCode="";
		}
		if("null".equals(ActiveType)){
			ActiveType="";
		}
		if("null".equals(FunctionCode)){
			FunctionCode="";
		}
		if("null".equals(CustomerCode)){
			CustomerCode="";
		}
		if("null".equals(BusinessCode)){
			BusinessCode="";
		}
		if("null".equals(IsBatch)){
			IsBatch="";
		}
		if("null".equals(ScanFlag)){
			ScanFlag="";
		}
		if("null".equals(AuthorizCode)){
			AuthorizCode="";
		}
		if("null".equals(ImgCode)){
			ImgCode="";
		}
		if("null".equals(UserCode)){
			UserCode="";
		}
		if("null".equals(OrgCode)){
			OrgCode="";
		}
		if("null".equals(BatchCode)){
			BatchCode="";
		}
		if("null".equals(BarCode)){
			BarCode="";
		}
		if("null".equals(FlowFlag)){
			FlowFlag="";
		}
		if("null".equals(FileFlag)){
			FileFlag="";
		}
		if("null".equals(CheckFlag)){
			CheckFlag="";
		}
		if("null".equals(PrintType)){
			PrintType="";
		}
		StringBuilder jsonStr = new StringBuilder();
		if(PrintType==null||PrintType==""){
			jsonStr.append("{");
		}else if(PrintType.equals("01")){
			jsonStr.append("{\"typeCode\":\"客户号流水号\",");
		}else if(PrintType.equals("03")){
			jsonStr.append("{\"typeCode\":\"业务号流水号\",");
		}else if(PrintType.equals("04")){
			jsonStr.append("{\"typeCode\":\"default\",");
		}
		jsonStr.append("\"username\":\""+username+"\",");
		jsonStr.append("\"password\":\""+password+"\",");
		jsonStr.append("\"ucpType\":\""+ucpType+"\",");
		jsonStr.append("\"url\":\""+url+"\",");
		jsonStr.append("\"SystemCode\":\""+SystemCode+"\",");
		jsonStr.append("\"FunctionCode\":\""+FunctionCode+"\",");
		jsonStr.append("\"CustomerCode\":\""+CustomerCode+"\",");
		jsonStr.append("\"BusinessCode\":\""+BusinessCode+"\",");
		jsonStr.append("\"BatchCode\":\""+BatchCode+"\",");
		jsonStr.append("\"OrgCode\":\""+OrgCode+"\",");
		jsonStr.append("\"UserCode\":\""+UserCode+"\",");
		jsonStr.append("\"ImgCode\":\""+ImgCode+"\",");
		jsonStr.append("\"AuthorizCode\":\""+AuthorizCode+"\",");
		jsonStr.append("\"ScanFlag\":\""+ScanFlag+"\",");
		jsonStr.append("\"IsBatch\":\""+IsBatch+"\",");
		jsonStr.append("\"CheckFlag\":\""+CheckFlag+"\",");
		jsonStr.append("\"FileFlag\":\""+FileFlag+"\",");
		jsonStr.append("\"FlowFlag\":\""+FlowFlag+"\",");
		jsonStr.append("\"BarCode\":\""+BarCode+"\",");
		jsonStr.append("\"PrintType\":\""+PrintType+"\",");
		jsonStr.append("\"ActiveType\":\""+ActiveType+"\",");


		jsonStr.append("\"last\":\"last\"}");
		PrintWriter writer=null;
		try{
			response.setCharacterEncoding("utf-8");
			response.setContentType("application/json");
			writer= response.getWriter();
			writer.write(jsonStr.toString());
		}catch(IOException e){
			e.printStackTrace();
		}

	}



}
