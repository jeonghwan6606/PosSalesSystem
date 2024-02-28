package com.pos.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.pos.vo.ClientVo;
import com.pos.vo.MembVo;
import com.pos.vo.ProdVo;
import com.pos.vo.SalesPayVo;
import com.pos.vo.SalesProdVo;
import com.pos.vo.SalesVo;
import com.pos.vo.VaultCashVo;
import com.pos.dao.PosDao;

@Repository("pDao")
public class PosDaoImpl implements PosDao { 
	
	@Autowired
	SqlSession sqlsession;

		// 상품 조회
		@Override
		public Map<String, Object> salesProdOne(String barcode){
			return sqlsession.selectOne("pos.selectProdOne",barcode);
			
		}
		
		// 상품 조회
		@Override
		public ProdVo selectProd(String barcode){
			return sqlsession.selectOne("pos.selectProd",barcode);
			
		}
		
		// 시재금 등록
		@Override
		public int InsertPosMoney(Map<String, String> salesData) {
			return sqlsession.insert("pos.InsertPosMoney", salesData);
		}
		
		@Override
	    public void insertSales(Map<String, String> salesData) {
		  sqlsession.insert("pos.InsertSales", salesData);
	    }
		
		@Override
		public void insertSalesProduct(SalesProdVo salesProduct) {
			sqlsession.insert("pos.InsertSalesProduct", salesProduct);
		}
		
		@Override
	    public void insertSalesPayment(SalesProdVo salesProduct) {
		  sqlsession.insert("pos.InsertSalesPayment", salesProduct);
	    }

		
		// 간이회원등록 
		@Override
		public int SimInsertCust(Map<String,String> map) {
			
			return sqlsession.insert("pos.SimInsertCust", map);
		}
		
		@Override
		public int insertCust(Map<String,String> map) {
			
			return sqlsession.insert("pos.insertCust", map);
		}
		
		// 중복검사(cust)
		@Override
		public int checkDuplicateCust(Map<String,String> param) {
				//Map<String, String> params = new HashMap<String, String>();
		       // params.put("membNm", membNm);
		        //params.put("phNo", phNo);
			return sqlsession.selectOne("pos.checkDuplicateCust", param);
		}
		
		@Override
	    public List<Map<String, Object>> selectSales(Map<String, Object> map) {
			
			 System.out.println("salessData: "+ sqlsession.selectList("pos.selectSales", map));
	        return sqlsession.selectList("pos.selectSales", map);
	    }

	    @Override
	    public List<Map<String, Object>> salesProdListBySalesSerNo(String salesSerNo,String salesTy) {
	    	Map<String,String> param = new HashMap<>();
	    	 param.put("serNo", salesSerNo);
	    	 param.put("salesTy", salesTy);
	        return sqlsession.selectList("pos.selectSalesProduct", param);
	    }
	    
	    
	    @Override
	    public void updateCancelTypeForFullCancel(String salesSerNo) {
	    	sqlsession.update("pos.updateCancelTypeForFullCancel", salesSerNo);
	    }

	    @Override
	    public void updateCancelType(Map<String, Object> cancelInfo) {
	    	sqlsession.update("pos.updateCancelType", cancelInfo);
	    }
	    
	    @Override
	    public int countSalesProdByTransTy(String salesSerNo) {
	        return sqlsession.selectOne("pos.countSalesProdByTransTy", salesSerNo);
	    }
	    
	    @Override
	    public String selectPayAmtBySerNo(String salesSerNo, String serNo) {
	    	Map<String,String> param = new HashMap<String,String>();
	    	param.put("salesSerNo", salesSerNo);
	    	param.put("serNo", serNo);
	    			
	        return sqlsession.selectOne("pos.selectPayAmtBySerNo", param);
	    }
	    
	   
	    
	    @Override
	    public List<Map<String, Object>> selectClient(Map<String, String> param) {
	        // 여기서 데이터베이스에 쿼리를 실행하여 결과를 맵 형태로 반환
	    	return sqlsession.selectList("pos.selectClient",param);
	    }
	    
	    @Override
	    public String calculatePos() {
	        return sqlsession.selectOne("pos.calculatePos");
	    }
	    
	    @Override
	    public List<Map<String, Object>> selectProductType() {
			
			 System.out.println("selectProductType: "+ sqlsession.selectList("pos.selectProductType"));
	        return sqlsession.selectList("pos.selectProductType");
	    }
	    
	    @Override
	    public int getMaxProdCd() {
	        return sqlsession.selectOne("pos.getMaxProdCd");
	    }
	    
		 // 간이회원등록 
		@Override
		public int insertProduct(Map<String,String> map) {
			
			return sqlsession.insert("pos.insertProduct", map);
		}
		
		// 중복검사(cust)
		@Override
		public int checkDuplicateProduct(String PROD_CD, String BAR_CODE) {
				Map<String, String> params = new HashMap<String, String>();
		        params.put("PROD_CD", PROD_CD);
		        params.put("BAR_CODE", BAR_CODE);
			return sqlsession.selectOne("pos.checkDuplicateProduct", params);
		}
		
		@Override
	    public int getMaxClient(){
	        return sqlsession.selectOne("pos.getMaxClient");
	    }
		@Override
		public int checkDuplicateClient(String CLIENT_NO, String BUSI_NO) {
				Map<String, String> params = new HashMap<String, String>();
		        params.put("CLIENT_NO", CLIENT_NO);
		        params.put("BUSI_NO", BUSI_NO);
			return sqlsession.selectOne("pos.checkDuplicateClient", params);
		}
		 // 간이회원등록 
		@Override
		public int insertClient(Map<String,String> map) {
			
			return sqlsession.insert("pos.insertClient", map);
		}
		
		@Override
	    public int selectSerNoByTransTy(String salesSerNo, String serNo) {
			Map<String, String> params = new HashMap<String, String>();
	        params.put("salesSerNo", salesSerNo);
	        params.put("serNo", serNo);
			
			return sqlsession.selectOne("pos.selectSerNoByTransTy", params);
	    }    
		 
		@Override
	    public List<Map<String, Object>> selectMembList(Map<String, String> param) {
	        // 여기서 데이터베이스에 쿼리를 실행하여 결과를 맵 형태로 반환
	    	return sqlsession.selectList("pos.selectMembList",param);
	    }
		
		 @Override
	    public int countMemberByMembSerNo(String membSerNo) {
	        return sqlsession.selectOne("pos.countMemberByMembSerNo", membSerNo);
	    }
		 
		@Override
	    public int getMaxMembNo() {
	        return sqlsession.selectOne("pos.getMaxMembNo");
	    }
		
		
		@Override
	    public List<String> selectSerNoBySalesSerNo(String salesSerNo) {
	        return sqlsession.selectList("pos.selectSerNoBySalesSerNo", salesSerNo);
	    }

	    @Override
	    public String getSalesAmtBySerNo(Map<String, Object> paramMap) {
	        return sqlsession.selectOne("pos.getSalesAmtBySerNo", paramMap);
	    }
	    
	    @Override
		public void updatedMembPoint(Map<String, String> paramMap) {
	    	
	    	System.out.println(paramMap);
			sqlsession.update("pos.updateMemberPoint", paramMap);
			return ;
	    }
}
