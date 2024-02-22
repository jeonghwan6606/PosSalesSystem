package com.pos.vo;

public class ProdClsVo {
	String PROD_CLS_CD;
	String PROD_CLS_NM;
	String REMARKS;
	
	@Override
	public String toString() {
		return "ProdClsVo [PROD_CLS_CD=" + PROD_CLS_CD + ", PROD_CLS_NM=" + PROD_CLS_NM + ", REMARKS=" + REMARKS
				+ ", getPROD_CLS_CD()=" + getPROD_CLS_CD() + ", getPROD_CLS_NM()=" + getPROD_CLS_NM()
				+ ", getREMARKS()=" + getREMARKS() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}
	public String getPROD_CLS_CD() {
		return PROD_CLS_CD;
	}
	public void setPROD_CLS_CD(String pROD_CLS_CD) {
		PROD_CLS_CD = pROD_CLS_CD;
	}
	public String getPROD_CLS_NM() {
		return PROD_CLS_NM;
	}
	public void setPROD_CLS_NM(String pROD_CLS_NM) {
		PROD_CLS_NM = pROD_CLS_NM;
	}
	public String getREMARKS() {
		return REMARKS;
	}
	public void setREMARKS(String rEMARKS) {
		REMARKS = rEMARKS;
	}

}
