package com.pos.service;

import java.util.List;
import java.util.Map;

import com.pos.vo.ClientVo;
import com.pos.vo.MembVo;
import com.pos.vo.ProdVo;
import com.pos.vo.SalesPayVo;
import com.pos.vo.SalesProdVo;
import com.pos.vo.SalesVo;
import com.pos.vo.VaultCashVo;

public interface PosService {
	
	
	 //pos화면 상품조회
	 Map<String, Object> salesProdOne(String barcode);
	
	
	 void insertPosMoney(Map<String, String> salesData);
	
	 void insertSales(Map<String,String> salesData,List<Map<String,String>> salesList);
	 
	 int SimInsertCust(Map<String, String> param);
	 
	 List<Map<String, Object>> selectSales(Map<String, Object> paramMap);

	 List<Map<String, Object>> salesProdListBySalesSerNo(String salesSerNo,String salesTy);
	 
	 int cancelSales(String salesSerNo, String[] serNos, boolean isFullCancel);
	 
	 
	 List<Map<String, Object>> selectClient(Map<String, String> param);
	 
	 List<Map<String, Object>> selectProductType();
	 
	 int insertProduct(Map<String, String> param);
	 
	 
	 String calculatePos();
	 int getMaxProdCd();
	 
	 int getMaxClient();
	 int insertClient(Map<String,String> map);
	 
	 int selectSerNoByTransTy(String salesSerNo, String serNo);
	 List<Map<String, Object>> selectMembList(Map<String, String> param);
	 int countMemberByMembSerNo(String membSerNo);
	  
	 int getMaxMembNo(); 
	  
	 int insertCust(Map<String,String> map);
	 //--------------------------------조회페이지--------------------------------------
	 List<Map<String, Object>> selectProduct(Map<String,String> map);
	/*
	// 회원등록
	String InsertPosCust(MembVo memVo);
	
	// 거래처등록
	String InsertManagement(ClientVo clientVo);
	
	// 상품등록
	String InsertProduct(ProdVo proVo);

	
	// 최종금액 
	String toatalMoney(VaultCashVo cashVo);
	
	// 계산
	void InsertSales(SalesVo salesVo, SalesProdVo prodVo, SalesPayVo payVo);
	
	// 날짜별 판매 조회
	List<Map<String, Object>> selectSales(List<Map<String, Object>> selectSales);
	
	// 판매상품 상세 조회
	List<Map<String, Object>> selectOne(List<Map<String, Object>> selectOne);
	
	// 고객 정보 조회
	List<Map<String, Object>> selectMember(List<Map<String, Object>> selectMember);
	*/
	
}
