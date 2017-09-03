/**
 * 
 */
package starter.cfsrest.bean;

import org.springframework.stereotype.Component;

/**
 * @author JackIce
 * @date  2016年6月8日
 * @description 
 */
@Component
public class CFSLogBean {
	private String ID;
	private String BIS_SYS_CODE;
	private String BIS_FUN_COD;
	private String IMG_NAME;
	private String ORG_CODE;
	private String OPERATE_PERSON;
	private String OPERATE_DATE;
	private String REMARK;
	private String BATCH_CODE;
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getBIS_SYS_CODE() {
		return BIS_SYS_CODE;
	}
	public void setBIS_SYS_CODE(String bIS_SYS_CODE) {
		BIS_SYS_CODE = bIS_SYS_CODE;
	}
	public String getBIS_FUN_COD() {
		return BIS_FUN_COD;
	}
	public void setBIS_FUN_COD(String bIS_FUN_COD) {
		BIS_FUN_COD = bIS_FUN_COD;
	}
	public String getIMG_NAME() {
		return IMG_NAME;
	}
	public void setIMG_NAME(String iMG_NAME) {
		IMG_NAME = iMG_NAME;
	}
	public String getORG_CODE() {
		return ORG_CODE;
	}
	public void setORG_CODE(String oRG_CODE) {
		ORG_CODE = oRG_CODE;
	}
	public String getOPERATE_PERSON() {
		return OPERATE_PERSON;
	}
	public void setOPERATE_PERSON(String oPERATE_PERSON) {
		OPERATE_PERSON = oPERATE_PERSON;
	}
	public String getOPERATE_DATE() {
		return OPERATE_DATE;
	}
	public void setOPERATE_DATE(String oPERATE_DATE) {
		OPERATE_DATE = oPERATE_DATE;
	}
	public String getREMARK() {
		return REMARK;
	}
	public void setREMARK(String rEMARK) {
		REMARK = rEMARK;
	}
	public String getBATCH_CODE() {
		return BATCH_CODE;
	}
	public void setBATCH_CODE(String bATCH_CODE) {
		BATCH_CODE = bATCH_CODE;
	}
	public CFSLogBean(String iD, String bIS_SYS_CODE, String bIS_FUN_COD,
			String iMG_NAME, String oRG_CODE, String oPERATE_PERSON,
			String oPERATE_DATE, String rEMARK, String bATCH_CODE) {
		super();
		ID = iD;
		BIS_SYS_CODE = bIS_SYS_CODE;
		BIS_FUN_COD = bIS_FUN_COD;
		IMG_NAME = iMG_NAME;
		ORG_CODE = oRG_CODE;
		OPERATE_PERSON = oPERATE_PERSON;
		OPERATE_DATE = oPERATE_DATE;
		REMARK = rEMARK;
		BATCH_CODE = bATCH_CODE;
	}
	public CFSLogBean() {
		super();
	}
	@Override
	public String toString() {
		return "CFSLogBean [ID=" + ID + ", BIS_SYS_CODE=" + BIS_SYS_CODE
				+ ", BIS_FUN_COD=" + BIS_FUN_COD + ", IMG_NAME=" + IMG_NAME
				+ ", ORG_CODE=" + ORG_CODE + ", OPERATE_PERSON="
				+ OPERATE_PERSON + ", OPERATE_DATE=" + OPERATE_DATE
				+ ", REMARK=" + REMARK + ", BATCH_CODE=" + BATCH_CODE + "]";
	}
	
}	
