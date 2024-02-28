package com.pos.dao;

import java.util.List;
import java.util.Map;

import com.pos.vo.ClientVo;
import com.pos.vo.MembVo;
import com.pos.vo.ProdVo;
import com.pos.vo.SalesPayVo;
import com.pos.vo.SalesProdVo;
import com.pos.vo.SalesVo;
import com.pos.vo.VaultCashVo;

public interface PosDao {
	
	// 상품 조회
	ProdVo selectProd(String barcode);
	Map<String, Object> salesProdOne(String barcode);
	int InsertPosMoney(Map<String, String> salesData);
	void insertSales(Map<String, String> salesData);
	void insertSalesProduct(SalesProdVo salesProduct);
	void insertSalesPayment(SalesProdVo salesProduct);
	int SimInsertCust(Map<String,String> map);
	int insertCust(Map<String,String> map);
	int checkDuplicateCust(Map<String,String> param);
	String selectPayAmtBySerNo(String salesSerNo, String serNo);
	List<Map<String, Object>> selectSales(Map<String, Object> map);

	List<Map<String, Object>> salesProdListBySalesSerNo(String salesSerNo,String salesTy);
	
	 // 판매 취소 관련 메서드
    void updateCancelTypeForFullCancel(String salesSerNo);
    void updateCancelType(Map<String, Object> cancelInfo);
    int countSalesProdByTransTy(String salesSerNo);
    
    List<Map<String, Object>> selectClient(Map<String, String> param);
    
    String calculatePos();
    List<Map<String, Object>> selectProductType();
    int getMaxProdCd();
    
    int insertProduct(Map<String,String> param);
    int checkDuplicateProduct(String PRODUCT_CD, String BAR_CODE);
    
    int getMaxClient();
    int checkDuplicateClient(String CLIENT_NO, String BUSI_NO);
    
    int insertClient(Map<String,String> map);
    
    int selectSerNoByTransTy(String salesSerNo, String serNo);
    List<Map<String, Object>> selectMembList(Map<String, String> param);
    
    int countMemberByMembSerNo(String membSerNo);
    
    int getMaxMembNo();
    
    List<String> selectSerNoBySalesSerNo(String salesSerNo);
    String getSalesAmtBySerNo(Map<String, Object> paramMap);
	
    void updatedMembPoint(Map<String,String> map);
    /*
	// 회원등록
	int InsertCust(MembVo memVo);
	// 거래처 등록
	int InsertClient(ClientVo clientVo);
	// 상품등록 
	int InsertProduct(ProdVo proVo);
	// 시재금 등록
	int InsertPosMoney(VaultCashVo cashVo);
	// 시재금 최종 금액
	String TotalMoney(VaultCashVo cashVo);
	// 계산
	int InsertSales(SalesVo SalesVo);
	// 상품테이블에 등록
	int InsertProd(SalesProdVo prodVo);
	// pay 테이블에 등록
	int InsertPay(SalesPayVo payVo);
	
	// 날짜별 판매 조회
	List<Map<String, Object>> selectSales(List<Map<String, Object>> selectSales);
	// 판매상품 상세 조회
	List<Map<String, Object>> selectOne(List<Map<String, Object>> selectOne);
	// 고객 정보 조회
	List<Map<String, Object>> selectMember(List<Map<String, Object>> selectOne);
	*/
}
