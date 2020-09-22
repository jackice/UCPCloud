/**
 * 
 */
package starter.cfsrest.bean;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author JackIce
 * @date  2016年5月30日
 * @description 
 */
public class Constant {
	public static final String CFS_MOBILE__SYSTEM="mobile_system";
	public static final String CFS_INDEX_REQUILD_NAME="name";
	public static final String NODE_ATTR_NAME="code";
	public static final String XML_ATTRANDVALUE_MAP_NAME="attr_list";
	public static final String XML_SPECIAL_ATTRANDVALUE_LIST_NAME="special_list";
	
	public static final String CFS_XMLROOTNODE_NAME="message";
	public static final String CFS_CHECK_FLAG="CK_FLAG";
	public static final String CFS_SYS_CODE_KEY="sys_code";
	public static final String CFS_FUN_CODE_KEY="fun_code";
	public static final String CFS_CUSTOM_CODE_KEY="custom_code";
	public static final String CFS_BUSS_ID_KEY="buss_id";
	public static final String CFS_OP_CODE_KEY="op_code";
	public static final String CFS_FLAG_KEY="flag";
	public static final String CFS_STAGE_FLAG_KEY="stage_flag";
	public static final String CFS_UNCLASSIFIED_FOLDER_NAME = "未分类";
	public static final String CFS_EMPTY_STRING = "";
	public static final String CFS_DOCUMENTS_STRING = "documents";
	public static final String CFS_COMPRESS_FILE_PREFIX = "CFS";
	public static final String CFS_COMPRESS_FILE_POSTFIX = ".zip";
	public static final String CFS_BACKSLASH_DOUBLE = File.separator;
	public static final String CFS_CHECK_FLAG_VALUE_ONE = "1";
	public static final String CFS_CHECK_FLAG_VALUE_TWO = "2";
	public static final String CFS_CHECK_FLAG_VALUE_THREE = "3";
	
	public static final String CFS_DATA_INDEX = "cfs_data";
	public static final String CFS_ORG_INFO_TYPE = "cfs_orginfo";
	public static final String CFS_DATA_STAGE_TYPE = "cfs_stage";
	public static final String CFS_MASTERINDEX_TYPE="cfs_master";
	public static final String CFS_TREEINDEX_TYPE="cfs_tree";
	public static final String CFS_SERVICEINDEX_TYPE="cfs_service";
	public static final String CFS_UPDATEINDEX_TYPE="cfs_update";
	public static final String CFS_COPYEINDEX_TYPE="cfs_copy";
	public static final String CFS_LOGSINDEX_TYPE="cfs_logs";
	
	public static final String CFS_IMG_RESULT_KEY="img_result";
	public static final String CFS_IMG_ID="img_id";
	public static final String CFS_RESULT_REMARK_KEY="img_remark";
	public static final String CFS_IMG_FILE="img_file";
	public static final String CFS_IMG_FILE_STAGE_FLAG="stage_flag";

	public static final String CFS_STAGE_FLAG_S0="S0";
	public static final String CFS_STAGE_FLAG_S1="S1";
	public static final String CFS_STAGE_FLAG_S2="S2";
	public static final String CFS_STAGE_FLAG_S3="S3";
	public static final String CFS_STAGE_FLAG_S4="S4";
	public static final String CFS_TYPE_CODE_13001003="13001003";
	public static final String CFS_TYPE_CODE_12001001003="12001001003";
	public static final String CFS_TYPE_CODE_12003001003="12003001003";
	
	public static final String CFS_OPERATION_IMG_CREATE="create";
	public static final String CFS_OPERATION_IMG_COUNT="img_count";
	public static final String CFS_RES_IMG_COUNT_IMGLIST="img_list";
	public static final String CFS_OPERATION_IMG_COPY="img_copy";
	public static final String CFS_OPERATION_IMG_UPDATE="img_update";
	public static final String CFS_OPERATION_UPDATE_STAGE="update_stage";
	public static final String CFS_OPERATION_IMG_API_DELETE="img_api_delete";
	public static final String CFS_OPERATION_TRANS_RES="trans_res";
	public static final String CFS_OPERATION_TRANS_error="trans_error";
	public static final String CFS_COUNT_FILED_NAME="TYPE_CODE";
	public static final String CFS_QLCXD_BUSINESS="QLCXD_BUSINESS";
	public static final String CFS_QLCXD_CUSTOMERS="QLCXD_CUSTOMER";
	
	public static final String CFS_IMG_TYPE="img_type";
	public static final String CFS_SYSCODE="syscode";
	public static final String CFS_FUNCODE="funcode";
	public static final String CFS_VERSION="version";
	public static final String CFS_SIDEFLAG="sideflag";
	public static final String CFS_MAIN_FLAG="mainflag";
	public static final String CFS_IMG_NAME="name";
	public static final String CFS_BATCH_FLG="batch_flg";
	public static final String CFS_BATCH_CODE="batch_code";
	public static final String CFS_ORGAN_NO="organ_no";
	public static final String CFS_CUS_ID="cusID";
	public static final String CFS_SOURCE_FLAG="source_flag";
	public static final String CFS_TRANS_STATE="trans_state";
	public static final String CFS_SCAN_TIME="scan_time";
	public static final String CFS_ACCEPT_NO="acceptNo";
	public static final String CFS_OPERATOR_ID="operator_id";
	public static final String CFS_SPLIT_FLAG="@code:";
	public static final String CFS_API_DELETE="img_api_delete";
	
	public static final int CFS_FUNNUMBER_ZERO=0;
	public static final int CFS_FUNNUMBER_ONE=1;
	public static final int CFS_FUNNUMBER_TWO=2;
	public static final int CFS_FUNNUMBER_THREE=3;
	public static final int CFS_FUNNUMBER_FOUR=4;
	public static final int CFS_FUNNUMBER_FIVE=5;
	public static final int CFS_FUNNUMBER_SIX=6;
	public static final int CFS_FUNNUMBER_SEVEN=7;
	public static final String CFS_DOCUMENT_ID="_id";
	public static final String CFS_TREE_MSG_CODE="msg_code";
	public static final String CFS_TREE_MSG_NAME="msg_name";
	public static final String CFS_IMAGE_POSTFIX=".jpg";
	
	
	public static final String CFS_COMMON_DIGIT_STRING_ONE="1";
	public static final String CFS_COMMON_DIGIT_STRING_ZERO="0";
	public static final String CFS_COMMON_DIGIT_STRING_THREE="3";
	public static final String CFS_COMMON_DIGIT_STRING_SIX="6";
	
	public class WarmPrompt{
		public static final String CFS_ISFILE_OK = "归档成功";
		public static final String CFS_CANCEL_ISFILE_OK = "取消归档成功";
		public static final String CFS_UPDATE_FILE_OK = "修改归档成功";
		public static final String CFS_CANCEL_UPDATE_FILE_OK = "取消修改归档成功";
		public static final String CFS_COPY_FILE_OK = "拷贝归档成功";
		public static final String CFS_CANCEL_COPY_FILE_OK = "取消拷贝归档成功";
		public static final String CFS_UPDATE_STAGE_FILE_OK = "修改阶段标识成功";
		public static final String CFS_IS_COUNT_OK = "查询成功";
		public static final String CFS_IS_COUNT_FAIL = "查询失败";
	}
	public enum ApiResponse{
		NAME("name"),
		VERSION("version"),
		IMAGE_ID("img_id"),
		FOLDER_NAME("foldername");
		String str_name;

		public String getStr_name() {
			return str_name;
		}
		public void setStr_name(String str_name) {
			this.str_name = str_name;
		}
		private ApiResponse(String str_name) {
			this.str_name = str_name;
		}
		public static boolean isConstins(String str_name){
			ApiResponse[] values = ApiResponse.values();
			boolean isConstins = false;
			for (ApiResponse apiResponse : values) {
				if (apiResponse.getStr_name().equals(str_name)) {
					isConstins = true;
					break;
				}
			}
			return isConstins;
		}
		
	}
	
	public enum ApiResponseOfError{
		syscode("syscode"),
		funcode("funcode"),
		batch_code("batch_code"),
		organ_no("organ_no"),
		source_flag("source_flag"),
		operator_id("operator_id"),
		acceptNo("acceptNo"),
		cusID("cusID"),
		result("result");
		String str_name;

		public String getStr_name() {
			return str_name;
		}
		public void setStr_name(String str_name) {
			this.str_name = str_name;
		}
		private ApiResponseOfError(String str_name) {
			this.str_name = str_name;
		}
		public static boolean isConstins(String str_name){
			ApiResponseOfError[] values = ApiResponseOfError.values();
			boolean isConstins = false;
			for (ApiResponseOfError apiResponseOfError : values) {
				if (apiResponseOfError.getStr_name().equals(str_name)) {
					isConstins = true;
					break;
				}
			}
			return isConstins;
		}
		
	}
	
	public enum DeleteApiResponse{
		sys_code("sys_code"),
		fun_code("fun_code"),
		buss_id("buss_id"),
		img_id("img_id");
		String str_name;
		public String getStr_name() {
			return str_name;
		}
		public void setStr_name(String str_name) {
			this.str_name = str_name;
		}
		private DeleteApiResponse(String str_name) {
			this.str_name = str_name;
		}
		public static boolean isConstins(String str_name){
			DeleteApiResponse[] values = DeleteApiResponse.values();
			boolean isConstins = false;
			for (DeleteApiResponse deleteApiResponse : values) {
				if (deleteApiResponse.getStr_name().equals(str_name)) {
					isConstins = true;
					break;
				}
			}
			return isConstins;
		}
		
	}
	public enum ApiResponseOfPublic{
		syscode("syscode"),
		funcode("funcode"),
		uploadURL("uploadURL");
		String str_name;
		
		public String getStr_name() {
			return str_name;
		}
		public void setStr_name(String str_name) {
			this.str_name = str_name;
		}
		private ApiResponseOfPublic(String str_name) {
			this.str_name = str_name;
		}
		public static boolean isConstins(String str_name){
			ApiResponseOfPublic[] values = ApiResponseOfPublic.values();
			boolean isConstins = false;
			for (ApiResponseOfPublic apiResponseOfPublic : values) {
				if (apiResponseOfPublic.getStr_name().equals(str_name)) {
					isConstins = true;
					break;
				}
			}
			return isConstins;
		}
		
	}
	public enum FunNames{
		IMG_FILE(CFS_IMG_FILE,CFS_FUNNUMBER_ONE),
		IMG_COPY(CFS_OPERATION_IMG_COPY,CFS_FUNNUMBER_TWO),
		IMG_UPDATE(CFS_OPERATION_IMG_UPDATE,CFS_FUNNUMBER_THREE),
		IMG_COUNT(CFS_OPERATION_IMG_COUNT,CFS_FUNNUMBER_FOUR),
		UPDATE_STAGE(CFS_OPERATION_UPDATE_STAGE,CFS_FUNNUMBER_FIVE),
		IMG_API_DELETE(CFS_OPERATION_IMG_API_DELETE,CFS_FUNNUMBER_SIX),
		TRANS_RES(CFS_OPERATION_TRANS_RES,CFS_FUNNUMBER_SEVEN),
		OTHER(CFS_EMPTY_STRING,CFS_FUNNUMBER_ZERO);
		String funName;
		int funNumber;
		FunNames(String funName,int funNumber){
			this.funName = funName;
			this.funNumber = funNumber;
		}
		public static int getFunNumberByFunName(String funName){
			FunNames[] values = FunNames.values();
			int resultFunNumber = 0;
			for (FunNames funNames : values) {
				if (funNames.getFunName().equals(funName)) {
					resultFunNumber = funNames.getFunNumber();
					break;
				}else{
					resultFunNumber = 0;
				}
			}
			return resultFunNumber;
		}
		public String getFunName() {
			return funName;
		}
		public void setFunName(String funName) {
			this.funName = funName;
		}
		public int getFunNumber() {
			return funNumber;
		}
		public void setFunNumber(int funNumber) {
			this.funNumber = funNumber;
		}
		
	}
}
