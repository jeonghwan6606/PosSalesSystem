package com.pos.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.pos.dao.PosDao;
import com.pos.vo.ClientVo;
import com.pos.vo.MembVo;
import com.pos.vo.ProdVo;
import com.pos.vo.SalesPayVo;
import com.pos.vo.SalesProdVo;
import com.pos.vo.SalesVo;
import com.pos.vo.VaultCashVo;
import com.pos.service.PosService;

@Service
public class PosServiceImpl implements PosService {
	
	@Autowired(required = false)
	PosDao posDao;
	
	// 상품 조회
	@Override
	public Map<String, Object> salesProdOne(String barcode) {
		
		Map<String, Object> salesProdOne = posDao.salesProdOne(barcode);
		
		System.out.println(salesProdOne);
		
		return salesProdOne;
	}
	
	@Override
	public int getMaxProdCd(){	
		return posDao.getMaxProdCd();
	}
	
	@Override
	public List<Map<String, Object>> selectClient(Map<String, String> param){
		
		List<Map<String, Object>> clientList = posDao.selectClient(param);
		
		System.out.println(clientList);
		
		return clientList;
	}
	
	@Override
	public List<Map<String, Object>> selectProductType(){
		
		List<Map<String, Object>> productTypeList = posDao.selectProductType();
		
		System.out.println(productTypeList);
		
		return productTypeList;
	}
	
	
	@Override
	public String calculatePos(){
		return posDao.calculatePos();
	}
	
	
	@Override
	public void insertSales(Map<String,String> salesData, List<Map<String,String>> salesList) {
	    
		
		posDao.insertSales(salesData);
	    
	    // 판매일련번호를 꺼내옴
	    String salesSerNo = salesData.get("SALES_SER_NO");
	    
	    // SalesProdVo 타입의 리스트로 변환
	    List<SalesProdVo> salesProductList = convertToSalesProdVoList(salesList);

	    // 나머지 로직 및 DAO 호출 등을 수행
	    setSerNoForEachProduct(salesProductList);
	    
	    for (SalesProdVo salesProductVO : salesProductList) {
	        // 판매일련번호를 각 VO에 설정
	        salesProductVO.setSALES_SER_NO(salesSerNo);

	        // 판매상품 등록 DAO 호출
	        posDao.insertSalesProduct(salesProductVO);
	        
	        posDao.insertSalesPayment(salesProductVO);
	    } 
	    
	    insertPosMoney(salesData);
	    
	    posDao.updatedMembPoint(salesData);
	     
	}
	
	private void setSerNoForEachProduct(List<SalesProdVo> salesProductList) {
	    int serialNumber = 1;  // 시작 일련번호

	    for (SalesProdVo salesProductVO : salesProductList) {
	        salesProductVO.setSER_NO(String.valueOf(serialNumber));
	        serialNumber++;
	    }
	}
	
	private List<SalesProdVo> convertToSalesProdVoList(List<Map<String, String>> salesList) {
	    List<SalesProdVo> salesProdVoList = new ArrayList<SalesProdVo>();

	    for (Map<String, String> sale : salesList) {
	        String barcode = sale.get("BAR_CODE");
	        String qty = sale.get("QTY");
	        String salesAmt = sale.get("SALES_AMT");
	        String saleAmt = sale.get("SALE_AMT");
	        
	        ProdVo ProdVo = posDao.selectProd(barcode);
	        SalesProdVo salesProdVo = new SalesProdVo(); // SalesProdVo 객체 생성
	        
	        if (ProdVo != null) {
	        	// PROD_CD 설정
	        	salesProdVo.setPROD_CD(ProdVo.getPROD_CD());

	        	// QTY 설정
	        	salesProdVo.setQTY(qty);

	        	// SALES_PR 설정 (SELL_PR 값을 가져와서 설정)
	        	salesProdVo.setSALES_PR(ProdVo.getSELL_PR());
	        	        
	            salesProdVo.setSALES_AMT(salesAmt); // SALES_AMT 설정
	            salesProdVo.setSALE_AMT(saleAmt); // SALE_AMT 설정
	            
	            // 맵에서 바코드를 이용하여 조회된 상품을 SalesProdVo 객체로 변환하여 리스트에 추가
	            salesProdVoList.add(salesProdVo);
	        }
	    }

	    return salesProdVoList;
	}
	
	@Override
	public void insertPosMoney(Map<String, String> salesData) {
	    // 거스름돈과 받은 돈 변수명으로 구분
	    String changeAmount = salesData.get("CHANGE_AMOUNT");
	    String receivedAmount = salesData.get("RECEIVED_AMOUNT");

	    // 받은 돈이 null이거나 공백이면 insert하지 않음
	    if (receivedAmount != null && !receivedAmount.isEmpty() && changeAmount != null && !changeAmount.isEmpty()) {
	        // 상품 거래로 설정
	        salesData.put("CONTENTS", "상품결제");
	        
	        // 받은 돈과 거스름돈을 정수로 파싱하여 상품 거래 금액을 계산
	        int received = Integer.parseInt(receivedAmount);
	        int change = Integer.parseInt(changeAmount);
	        int productAmount = received - change;

	        // DEP_PAY_TY를 1로 설정하고 AMT로 상품 거래 금액을 설정
	        salesData.put("DEP_PAY_TY", "1");
	        salesData.put("AMT", String.valueOf(productAmount));
	        
	        // 데이터 삽입
	        posDao.InsertPosMoney(salesData);
	    }

	    return;
	}
	
	
	
	// 간이회원등록
	@Override
	public int SimInsertCust(Map<String,String> param) {
		
		String MEMB_NM = param.get("MEMB_NM");
		System.out.println("MEMB_NM:"+MEMB_NM);
		int MEMB_SER_NOs = posDao.getMaxMembNo();
		String MEMB_SER_NO = String.format("%06d", MEMB_SER_NOs); // 6자리로 패딩하여 문자열로 변환
		
		param.put("MEMB_SER_NO", MEMB_SER_NO);
		
		System.out.println("param: " + param);
		
	    String PH_NO = param.get("PH_NO");
		int existingCount = posDao.checkDuplicateCust(param);
		
		// 조회된 카운트가 0이면 회원가입 가능v
	    if (existingCount == 0) {
	        // 회원 등록
	        posDao.SimInsertCust(param);
	        System.out.println("회원 등록이 완료되었습니다.");
	    } else {
	        System.out.println("이미 존재하는 회원입니다. 회원 가입이 불가능합니다.");
	        return 0;
	    }

	    return 1;
		 
	}
	
	
	 @Override
	 public List<Map<String, Object>> selectSales(Map<String, Object> paramMap) {
		    List<Map<String, Object>> salesData = posDao.selectSales(paramMap);

		    // 새로운 리스트를 생성하여 업데이트된 데이터를 담을 준비
		    List<Map<String, Object>> updatedSalesData = new ArrayList<>();

		    // salesData의 각각의 데이터에 대해 작업 수행
		    for (Map<String, Object> salesRecord : salesData) {
		        // salesData의 각 데이터를 복제하여 수정할 데이터를 만듭니다.
		        Map<String, Object> updatedSalesRecord = new HashMap<>(salesRecord);

		        String salesSerNo = (String) salesRecord.get("SALES_SER_NO");

		        // PROD_TABLE에서 해당 SALES_SER_NO에 해당하는 SER_NO 조회
		        List<String> serNoList = posDao.selectSerNoBySalesSerNo(salesSerNo);

		        // TRANS_TY가 1인 값들의 SER_NO에 해당하는 데이터만 PAY 테이블에서 합계 구함
		        double totalSalesAmt = 0.0;
		        for (String serNo : serNoList) {
		            // 파라미터 생성
		            Map<String, Object> paramMapForAmt = new HashMap<>();
		            paramMapForAmt.put("salesSerNo", salesSerNo);
		            paramMapForAmt.put("serNo", serNo);
		            paramMapForAmt.put("salesTy", paramMap.get("salesTy"));

		            // 해당 SER_NO에 대한 AMT 조회
		            String salesAmtStr = posDao.getSalesAmtBySerNo(paramMapForAmt);
		            double salesAmt = Double.parseDouble(salesAmtStr);
		            totalSalesAmt += salesAmt;
		        }

		        // 새로운 값을 추가하여 업데이트된 레코드를 생성
		        updatedSalesRecord.put("TOTAL_SALES_AMT", totalSalesAmt);

		        // 업데이트된 레코드를 새로운 리스트에 추가
		        updatedSalesData.add(updatedSalesRecord);

		        // 디버깅 코드 추가
		        System.out.println("Updated Sales Record: " + updatedSalesRecord);
		    }

		    // 업데이트된 데이터 반환
		    return updatedSalesData;
		}

    @Override
    public List<Map<String, Object>> salesProdListBySalesSerNo(String salesSerNo,String salesTy) {
        return posDao.salesProdListBySalesSerNo(salesSerNo,salesTy);
    }
    
    
    @Override
    public int cancelSales(String salesSerNo, String[] serNos, boolean isFullCancel) {
        // 전체 취소인 경우 SALES_TBL의 해당 판매일련번호의 CANC_TY를 1로 업데이트
        Map<String, String> param = new HashMap<>();
        param.put("DEP_PAY_TY", "2");
        param.put("CONTENTS", "상품환불");

        String amt = "0"; // 초기값을 0으로 설정

        for (String serNo : serNos) {

            int check = posDao.selectSerNoByTransTy(salesSerNo, serNo);
            
            // 디버깅 코드 추가: check 변수 값 출력
            System.out.println("Check value for serNo " + serNo + ": " + check);

            // check가 0이 아니면 해당 serNo에 대해서는 작업을 수행하지 않습니다.
            if (check != 0) {
                continue;
            }

            Map<String, Object> cancelInfo = new HashMap<>();
            cancelInfo.put("salesSerNo", salesSerNo);
            cancelInfo.put("serNo", serNo);

            posDao.updateCancelType(cancelInfo);

            String payAmt = posDao.selectPayAmtBySerNo(salesSerNo, serNo);
            
            // 디버깅 코드 추가: payAmt 변수 값 출력
            System.out.println("Pay amount for serNo " + serNo + ": " + payAmt);

            // 각 반복에서 반환된 금액을 소수점을 제거한 후 누적하여 저장
            if (payAmt != null) {
                double amount = Double.parseDouble(payAmt);
                int intAmount = (int) amount; // 소수점 제거
                amt = String.valueOf(Integer.parseInt(amt) + intAmount);
            }
        }

        param.put("AMT", amt);
        posDao.InsertPosMoney(param);

        int count = posDao.countSalesProdByTransTy(salesSerNo);

        // If count is 0, execute full cancel
        if (count == 0) {
            posDao.updateCancelTypeForFullCancel(salesSerNo);
            
            return 2;
        }

        return 1;
    }
    
    @Override
    public int selectSerNoByTransTy(String salesSerNo, String serNo){
        return posDao.selectSerNoByTransTy(salesSerNo,serNo);
    }
      
 // 간이회원등록
 	@Override
 	public int insertProduct(Map<String,String> param) {
 		
 		String PROD_CD = param.get("PROD_CD");
 		System.out.println("PROD_CD:"+PROD_CD);
 		
 		String BAR_CODE = param.get("BAR_CODE");
 		System.out.println("BAR_CODE:"+BAR_CODE);
 		
 	    
 		int existingCount = posDao.checkDuplicateProduct(PROD_CD, BAR_CODE);
 		
 		// 조회된 카운트가 0이면 회원가입 가능
 	    if (existingCount == 0) {
 	        // 회원 등록
 	        posDao.insertProduct(param);
 	        System.out.println("상품등록이 완료되었습니다.");
 	    } else {
 	        System.out.println("이미 존재하는 상품입니다. 상품등록이 불가능합니다.");
 	        return 0;
 	    }

 	    return 1;
 		 
 	}
 	
 	@Override
	public int getMaxClient(){	
		return posDao.getMaxClient();
	}
	
 	
 	@Override
 	public int insertClient(Map<String,String> param) {
 		
 		String CLIENT_NO = param.get("CLIENT_NO");
 		System.out.println("CLIENT_NO:"+CLIENT_NO);
 		
 		String BUSI_NO = param.get("BUSI_NO");
 		System.out.println("BUSI_NO:"+BUSI_NO);
 		
 	    
 		int existingCount = posDao.checkDuplicateClient(CLIENT_NO, BUSI_NO);
 		
 		// 조회된 카운트가 0이면 회원가입 가능
 	    if (existingCount == 0) {
 	        // 회원 등록
 	        posDao.insertClient(param);
 	        System.out.println("거래처등록이 완료되었습니다.");
 	    } else {
 	        System.out.println("이미 존재하는 거래처입니다. 거래처등록이 불가능합니다.");
 	        return 0;
 	    }

 	    return 1;
 		 
 	}
 	
 	@Override
 	public int insertCust(Map<String,String> param) {
 		
 		String MEMB_SER_NO = param.get("MEMB_SER_NO");
 		System.out.println("MEMB_SER_NO:"+MEMB_SER_NO);
 		
 		String MOB_PH_NO = param.get("MOB_PH_NO");
 		System.out.println("MOB_PH_NO:"+MOB_PH_NO);
 		
 	    
 		int existingCount = posDao.checkDuplicateCust(param);
 		
 		// 조회된 카운트가 0이면 회원가입 가능
 	    if (existingCount == 0) {
 	        // 회원 등록
 	        posDao.insertCust(param);
 	        System.out.println("회원등록이 완료되었습니다.");
 	    } else {
 	        System.out.println("이미 존재하는 회원입니다. 등록이 불가능합니다.");
 	        return 0;
 	    }

 	    return 1;
 		 
 	}
 	
 	@Override
	public List<Map<String, Object>> selectMembList(Map<String, String> param){
		
		List<Map<String, Object>> membList = posDao.selectMembList(param);
		
		System.out.println(membList);
		
		return membList;
	}
 	
 	@Override
    public int countMemberByMembSerNo(String membSerNo) {
        return posDao.countMemberByMembSerNo(membSerNo);
    }
 	
 	
 	@Override
	public int getMaxMembNo(){	
		return posDao.getMaxMembNo();
	}
	
 	@Override
	public List<Map<String, Object>> selectProduct(Map<String, String> param){
		
		List<Map<String, Object>> productList = posDao.selectProduct(param);
		
		System.out.println(productList);
		
		return productList;
	}
}
