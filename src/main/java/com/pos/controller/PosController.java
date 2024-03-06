package com.pos.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;

import com.cleopatra.json.JSONArray;
import com.cleopatra.json.JSONObject;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.spring.JSONDataView;
import com.cleopatra.spring.UIView;
import com.pos.dao.PosDao;
import com.pos.service.PosService;
import com.pos.vo.ClientVo;
import com.pos.vo.MembVo;
import com.pos.vo.ProdVo;
import com.pos.vo.SalesVo;
import com.pos.vo.VaultCashVo;

@Controller
public class PosController {

	@Autowired(required = false)
	PosService posService;
	
	@Autowired(required = false)
	PosDao posDao;

	// 매핑 뒤에 .clx가 생략되어 있음.
	// URL 표시는 /PosMain.clx
	// 포스메인 페이지 호출

	// 포스 메인화면 - PosMain.clx
	// 판매관리 - PosSalesManagement.clx
	// 시재금 - PosMoney.clx
	// 상품관리 - PosProductRegist1.clx
	// 거래처관리 - PosAccountManagement.clx
	// 회원관리 - PosCust.clx

	@RequestMapping("/PosMain.do")
	public UIView posMain(HttpServletRequest req, HttpServletResponse res, DataRequest dataReq) throws Exception {
	    // 취소된 품목들 배열을 추출
	    String cancelledItemsJson = req.getParameter("cancelledItems");
	    List<Map<String, Object>> cancelledItemsList = new ArrayList<>();
	    if (cancelledItemsJson != null) {
	       JSONArray cancelledItemsArray = new JSONArray(cancelledItemsJson);
	        // JSONArray를 List<Map<String, Object>>로 변환
	        for (int i = 0; i < cancelledItemsArray.length(); i++) {
	            JSONObject item = cancelledItemsArray.getJSONObject(i);
	            Map<String, Object> itemMap = new HashMap<>();
	            itemMap.put("barcode", item.getString("barcode"));
	            itemMap.put("qty", item.getInt("qty"));
	            // 필요한 항목들을 추가로 처리
	            cancelledItemsList.add(itemMap);
	        }
	        
	     // 디버깅을 위한 출력
		    System.out.println("Cancelled Items: " + cancelledItemsList);
		    
		    // 리스트를 다시 맵으로 래핑하여 initParam으로 전달
		    Map<String, List<Map<String, Object>>> initParams = new HashMap<>();
		    initParams.put("cancelledItems", cancelledItemsList);
		    
		    // 포워드하여 PosMain.clx로 요청 전달
		    return new UIView("/ui/PosMain.clx", initParams);  
	    } 
	    // 포워드하여 PosMain.clx로 요청 전달
	    return new UIView("/ui/PosMain.clx");
	}

	@RequestMapping("/GetProdOne.do")
	public View getProdOne(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {

		String barcode = dataRequest.getParameter("searchValue");

		System.out.println("barcode: " + barcode);

		// PosService를 통해 상품 정보 가져오기
		Map<String, Object> product = posService.salesProdOne(barcode);

		System.out.println("product: " + product);

		// 응답 데이터셋에 상품 정보 추가
		dataRequest.setResponse("ds1", product);

		return new JSONDataView();

	}
	
	@RequestMapping("/GetInitDataProd.do")
	public View getProdType(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {

		// PosService를 통해 상품 정보 가져오기
		List<Map<String, Object>> productType = posService.selectProductType();

		System.out.println("productType: " + productType);

		int PROD_SEQ_NOs = posService.getMaxProdCd();
		String PROD_SEQ_NO = String.format("%06d", PROD_SEQ_NOs); // 6자리로 패딩하여 문자열로 변환
		
		// 응답 데이터셋에 상품 정보 추가
		dataRequest.setResponse("productType", productType);
		dataRequest.setResponse("PROD_SEQ_NO", PROD_SEQ_NO);

		return new JSONDataView();

	}
	
	
	

	@RequestMapping("/GetMembOne.do")
	public View getMembOne(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {

		JSONObject requestData = dataRequest.getRequestObject();

		String phNo = requestData.getString("PH_NO");
		String membNo = requestData.getString("MEMB_NO");
		String membNm = requestData.getString("MEMB_NM");

		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("PH_NO", phNo);
		paramMap.put("MEMB_NO", membNo);
		paramMap.put("MEMB_NM", "");

		System.out.println("param: " + paramMap);

		// PosService를 통해 상품 정보 가져오기
		List<Map<String, Object>> memb = posService.selectMembList(paramMap);

		System.out.println("memb: " + memb);

		// 응답 데이터셋에 상품 정보 추가
		dataRequest.setResponse("memb", memb);

		return new JSONDataView();

	}

	@RequestMapping(value = "/AddMember.do")
	public View addMember(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
		JSONObject requestData = dataRequest.getRequestObject();

		String phNo = requestData.getString("PH_NO");
		String idNo = requestData.getString("ID_NO");
		String membNm = requestData.getString("MEMB_NM");
		String busiNo = requestData.getString("BUSI_NO");

		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("MOB_PH_NO", phNo);
		paramMap.put("ID_NO", idNo);
		paramMap.put("MEMB_NM", membNm);
		paramMap.put("BUSI_NO", busiNo);

		// 파라미터 출력
		System.out.println("param: " + paramMap);

		int result = posService.SimInsertCust(paramMap);
		String message;
		String chkDuplicate;
		
		if (result == 0) {
			// 이미 회원 가입된 경우
			message = "이미 회원으로 등록되어 있습니다.";
			chkDuplicate = "0";
		} else {
			// 회원 가입 성공
			message = "회원 등록이 성공적으로 완료되었습니다.";
			chkDuplicate = "1";
		}
		
		
		// 응답 데이터셋에 메시지 추가
		dataRequest.setResponse("result", chkDuplicate);
		dataRequest.setResponse("message", message);
		// JSON 형식의 응답을 반환합니다.
		return new JSONDataView();
	}

	@RequestMapping("/AddSales.do")
	public View addSales(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {

		JSONObject requestData = dataRequest.getRequestObject();

		// 디버깅을 위해 요청된 데이터를 콘솔에 출력합니다.
		System.out.println("Received Request Data: " + requestData);

		String membNo = requestData.getString("membNo");
		
		
		String changeAmount = requestData.getString("changeAmount");
		String receivedAmount = requestData.getString("receivedAmount");
		String usedPoint = requestData.getString("usedPoint");
		
		JSONArray selectedDataArray = requestData.getJSONArray("selectedData");

		List<Map<String, String>> selectedDataList = new ArrayList<Map<String, String>>();

		// 선택된 각 행의 데이터를 맵으로 변환하여 리스트에 추가합니다.
		for (int i = 0; i < selectedDataArray.length(); i++) {
			JSONObject rowData = selectedDataArray.getJSONObject(i);
			Map<String, String> rowDataMap = new HashMap<>();
			rowDataMap.put("BAR_CODE", rowData.getString("BAR_CODE"));
			rowDataMap.put("QTY", rowData.getString("QTY"));
			rowDataMap.put("SALES_AMT", String.valueOf(rowData.getDouble("SALES_AMT")));
			rowDataMap.put("SALE_AMT", String.valueOf(rowData.getDouble("SALE_AMT")));// SALES_AMT가 숫자일 경우 getDouble()을
																						// 사용합니다.
			selectedDataList.add(rowDataMap);
		}

		// 디버깅을 위해 변환된 데이터를 콘솔에 출력합니다.
		System.out.println("Transformed Selected Data: " + selectedDataList.toString());

		// Map으로 파라미터 설정
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("MEMB_NO", membNo);
		paramMap.put("CHANGE_AMOUNT", changeAmount);
		paramMap.put("RECEIVED_AMOUNT", receivedAmount);
		paramMap.put("USED_POINT", usedPoint);

		// 디버깅을 위해 파라미터 데이터를 콘솔에 출력합니다.
		System.out.println("Parameter Map: " + paramMap.toString());

		// posService로 데이터 전달
		posService.insertSales(paramMap, selectedDataList);

		return new JSONDataView();
	}

	// 판매관리 페이지 호출
	@RequestMapping("/PosSalesManagement.do")
	public UIView posSalesManagement(HttpServletRequest req, HttpServletResponse res, DataRequest dataReq)
			throws Exception {
		return new UIView("/ui/PosSalesManagement.clx");
	}

	// 상품관리 페이지 호출
	@RequestMapping("/PosProductRegist1.do")
	public UIView posProductRegist1(HttpServletRequest req, HttpServletResponse res, DataRequest dataReq)
			throws Exception {
		return new UIView("/ui/PosProductRegist1.clx");
	}

	// 시재금 페이지 호출
	@RequestMapping("/PosMoney.do")
	public UIView posMoney(HttpServletRequest req, HttpServletResponse res, DataRequest dataReq) throws Exception {
		return new UIView("/ui/PosMoney.clx");
	}

	// 회원관리 페이지 호출
	@RequestMapping("/PosCust.do")
	public UIView posCust(HttpServletRequest req, HttpServletResponse res, DataRequest dataReq) throws Exception {
		return new UIView("/ui/PosCust.clx");
	}

	// 거래처관리 페이지 호출
	@RequestMapping("/PosAccountManagement.do")
	public UIView posAccountManagement(HttpServletRequest req, HttpServletResponse res, DataRequest dataReq)
			throws Exception {
		return new UIView("/ui/PosAccountManagement.clx");
	}

	// 상품관리1 -> 거래처 검색버튼 클릭 -> 상품관리2 popup
	@RequestMapping("/PosProductRegist2.do")
	public UIView posProductRegist2(HttpServletRequest req, HttpServletResponse res, DataRequest dataReq)
			throws Exception {
		return new UIView("/ui/PosProductRegist2.clx");
	}

	
	@RequestMapping("/searchMembPop.do")
	public UIView searchMembPop(HttpServletRequest req, HttpServletResponse res, DataRequest dataReq)
			throws Exception {
		return new UIView("/ui/searchMembPop.clx");
	}
	
	@RequestMapping(value = "/GetSalesData.do", method = RequestMethod.POST)
	public View getSalesData(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
		JSONObject requestData = dataRequest.getRequestObject();

		String phNo = requestData.getString("phNo");
		String barcode = requestData.getString("barcode");
		String salesTy = requestData.getString("salesTy");
		String transDtStart = requestData.getString("transDtStart");
		String transTmStart = requestData.getString("transTmStart");
		String transDtEnd = requestData.getString("transDtEnd");
		String transTmEnd = requestData.getString("transTmEnd");

		// Prepare parameters
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("phNo", phNo);
		paramMap.put("barcode", barcode);
		paramMap.put("salesTy", salesTy);
		paramMap.put("transDtStart", transDtStart);
		paramMap.put("transTmStart", transTmStart);
		paramMap.put("transDtEnd", transDtEnd);
		paramMap.put("transTmEnd", transTmEnd);

		System.out.println("paramMap: " + paramMap);

		// Call service to get sales data
		List<Map<String, Object>> salesData = posService.selectSales(paramMap);

		System.out.println("salessData: " + salesData);
		// Prepare response
		dataRequest.setResponse("salesData", salesData);

		return new JSONDataView();
	}

	@RequestMapping(value = "/GetSalesProduct.do", method = RequestMethod.POST)
	public View getSalesProduct(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
		String salesSerNo = dataRequest.getParameter("salesSerNo");
		String salesTy = dataRequest.getParameter("salesTy");
		
		System.out.println("salesSerNo: " + salesSerNo);

		// PosService를 통해 상품 정보 가져오기
		List<Map<String, Object>> salesProduct = posService.salesProdListBySalesSerNo(salesSerNo,salesTy);

		System.out.println("salesProduct: " + salesProduct);

		// Prepare response
		dataRequest.setResponse("salesProduct", salesProduct);

		return new JSONDataView();
	}

	@RequestMapping("/cancelSales.do")
	public View cancelSales(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
		JSONObject requestData = dataRequest.getRequestObject();

		
		String salesSerNo = requestData.getString("salesSerNos");

		System.out.println("salesSerNo: " + salesSerNo);

		// isFullCancel 값 추출
		boolean isFullCancel = requestData.getBoolean("isFullCancel");
		
		
		JSONArray serNosArray = requestData.getJSONArray("serNos");

		System.out.println("serNosArray: " + serNosArray);

		// serNos 배열을 String[] 배열로 변환
		String[] serNos = new String[serNosArray.length()];
		for (int i = 0; i < serNosArray.length(); i++) {
			serNos[i] = serNosArray.getString(i);

			
		}

		int cancel = posService.cancelSales(salesSerNo, serNos, isFullCancel);
			
		// 취소 처리 완료 후 응답 데이터 설정
		JSONObject responseData = new JSONObject();
		responseData.put("status", "success");
		
		dataRequest.setResponse("cancel", cancel);
		
		// 응답 반환
		return new JSONDataView();
	}
	
	
	@RequestMapping(value = "/GetClient.do", method = RequestMethod.POST)
	public View getClient(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
		
		System.out.println(dataRequest.getParameter("CLIENT_NM"));
		
		
		String CLIENT_NM = dataRequest.getParameter("CLIENT_NM");
		String CLIENT_NO = dataRequest.getParameter("CLIENT_NO");

		// Prepare parameters
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("CLIENT_NO", CLIENT_NO);
		paramMap.put("CLIENT_NM", CLIENT_NM);
		

		System.out.println("paramMap: " + paramMap);

		// Call service to get sales data
		List<Map<String, Object>> clientList = posService.selectClient(paramMap);

		System.out.println("clientList: " + clientList);
		// Prepare response
		dataRequest.setResponse("clientList", clientList);

		return new JSONDataView();
	}
	
	@RequestMapping(value = "/GetMemb.do", method = RequestMethod.POST)
	public View getMemb(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
		
		System.out.println(dataRequest.getParameter("MEMB_NM"));
		
		
		String MEMB_NM = dataRequest.getParameter("MEMB_NM");
		String PH_NO = dataRequest.getParameter("PH_NO");

		System.out.println("PH_NO:"+PH_NO);
		
		// Prepare parameters
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("MEMB_NM", MEMB_NM);
		paramMap.put("PH_NO", PH_NO);
		

		System.out.println("paramMap: " + paramMap);

		// Call service to get sales data
		List<Map<String, Object>> membList = posService.selectMembList(paramMap);

		System.out.println("membList: " + membList);
		// Prepare response
		dataRequest.setResponse("membList", membList);

		return new JSONDataView();
	}
	
	
	
	@RequestMapping("/GetMoney.do")
	public View getMoney(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
	
		String posMoney = posService.calculatePos();

		System.out.println("posMoney: " + posMoney);
		// Prepare response
		dataRequest.setResponse("posMoney", posMoney);

		return new JSONDataView();
	}
	
	@RequestMapping(value = "/CalculatePos.do", method = RequestMethod.POST)
	public View CalculatePos(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {

	    String AMT = dataRequest.getParameter("AMT");
	    String Contents = dataRequest.getParameter("CONTENTS");
	    String DEP_PAY_TY = dataRequest.getParameter("DEP_PAY_TY");

	    // 공백 처리
	    if (Contents == null) {
	        Contents = ""; // null이면 공백으로 설정
	    }

	    // Prepare parameters
	    Map<String, String> paramMap = new HashMap<>();
	    paramMap.put("AMT", AMT);
	    paramMap.put("CONTENTS", Contents);
	    paramMap.put("DEP_PAY_TY", DEP_PAY_TY);
	    
	    System.out.println("paramMap: " + paramMap);

		    // Call service to get sales data
		  int posMoney =  posDao.InsertPosMoney(paramMap);        

	    return new JSONDataView();
	}
	
	@RequestMapping(value = "/AddProduct.do")
	public View addProduct(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
		JSONObject requestData = dataRequest.getRequestObject();
		
		String PROD_CD = requestData.getString("PROD_CD");
		String PURC_PR = requestData.getString("PURC_PR");
		String PROD_CLS_CD = requestData.getString("PROD_CLS_CD");
		String PROD_NM = requestData.getString("PROD_NM");
		String SELL_PR = requestData.getString("SELL_PR");
		String SALE_PR = requestData.getString("SALE_PR");
		String PROD_ENG_NM = requestData.getString("PROD_ENG_NM");
		String ORIG_NAT = requestData.getString("ORIG_NAT");
		String BAR_CODE = requestData.getString("BAR_CODE");
		String CLIENT_NO = requestData.getString("CLIENT_NO");
		String COLOR = requestData.getString("COLOR");
		String PROD_SIZE = requestData.getString("SIZE");
		
		String MEM_POINT = requestData.getString("MEM_POINT");


	    // SALE_TY와 TAX_TY를 문자열로 받아옴
	    boolean saleTyStr = requestData.getBoolean("SALE_TY");
	    boolean taxTyStr = requestData.getBoolean("TAX_TY");

	  

	    // boolean 값을 1 또는 2로 변환
	    String saleTyValue = saleTyStr ? "1" : "2";
	    String taxTyValue = taxTyStr ? "2" : "1";
	 

		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("PROD_CD", PROD_CD);
		paramMap.put("PURC_PR", PURC_PR);
		paramMap.put("PROD_CLS_CD", PROD_CLS_CD);
		paramMap.put("PROD_NM", PROD_NM);
		paramMap.put("SELL_PR", SELL_PR);
		paramMap.put("SALE_PR", SALE_PR);
		paramMap.put("PROD_ENG_NM", PROD_ENG_NM);
		paramMap.put("ORIG_NAT", ORIG_NAT);
		paramMap.put("BAR_CODE", BAR_CODE);
		paramMap.put("CLIENT_NO", CLIENT_NO);
		paramMap.put("COLOR", COLOR);
		paramMap.put("PROD_SIZE", PROD_SIZE);
		
		paramMap.put("MEM_POINT", MEM_POINT);
		paramMap.put("SALE_OR_NOT", saleTyValue);
		paramMap.put("TAX_TY", taxTyValue);

		// 파라미터 출력
		System.out.println("param: " + paramMap);

		int result = posService.insertProduct(paramMap); // 수정된 부분, posService를 productService로 변경

		String message;
		if (result == 0) {
		    // 이미 등록된 경우
		    message = "이미 등록된 상품입니다.";
		} else {
		    // 상품 등록 성공
		    message = "상품 등록이 성공적으로 완료되었습니다.";
		}
		// 응답 데이터셋에 메시지 추가
		dataRequest.setResponse("message", message);

		// JSON 형식의 응답을 반환합니다.
		return new JSONDataView();
	}
	
	@RequestMapping("/GetInitDataClient.do")
	public View getClientInit(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {

		

		int CLIENT_SEQ_NOs = posService.getMaxClient();
		String CLIENT_SEQ_NO = String.format("%06d", CLIENT_SEQ_NOs); // 6자리로 패딩하여 문자열로 변환
		
		
		dataRequest.setResponse("CLIENT_SEQ_NO", CLIENT_SEQ_NO);

		return new JSONDataView();

	}
	
	@RequestMapping(value = "/AddClient.do")
	public View addClient(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
	    JSONObject requestData = dataRequest.getRequestObject();
	    
	    Map<String, String > paramMap = new HashMap<>();
	    paramMap.put("CLIENT_NO", requestData.getString("CLIENT_NO"));
	    paramMap.put("CLIENT_NM", requestData.getString("CLIENT_NM"));
	    paramMap.put("BUSI_NO", requestData.getString("BUSI_NO"));
	    paramMap.put("PERS_COP_TY", requestData.getString("PERS_COP_TY"));
	    paramMap.put("ID_NO", requestData.getString("ID_NO"));
	    paramMap.put("REPRES_NM", requestData.getString("REPRES_NM"));
	    paramMap.put("PH_NO", requestData.getString("PH_NO"));
	    paramMap.put("POST_NO", requestData.getString("POST_NO"));
	   	paramMap.put("ADDR", requestData.getString("ADDR"));
	    
	    // 디버깅 코드 추가
	    System.out.println("AddClient.do 요청 받음");
	    System.out.println("전달된 파라미터: " + paramMap);
	    
	    // 서비스에서 클라이언트 추가 메소드 호출하도록 수정
	    int result = posService.insertClient(paramMap);

	    String message;
	    if (result == 0) {
	        // 이미 등록된 경우
	        message = "이미 등록된 거래처입니다.";
	    } else {
	        // 클라이언트 등록 성공
	        message = "거래처 등록이 성공적으로 완료되었습니다.";
	    }
	    // 응답 데이터셋에 메시지 추가
	    dataRequest.setResponse("message", message);

	    // JSON 형식의 응답을 반환합니다.
	    return new JSONDataView();
	}
	
	@RequestMapping("/GetInitDataMemb.do")
	public View getMembInit(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {

		

		int MEMB_SEQ_NOs = posService.getMaxMembNo();
		String MEMB_SEQ_NO = String.format("%06d", MEMB_SEQ_NOs); // 6자리로 패딩하여 문자열로 변환
		
		
		dataRequest.setResponse("MEMB_SEQ_NO", MEMB_SEQ_NO);

		return new JSONDataView();

	}
	
	@RequestMapping(value = "/AddCust.do")
	public View addCust(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
	    JSONObject requestData = dataRequest.getRequestObject();
	    
	    Map<String, String > paramMap = new HashMap<>();
	    paramMap.put("MEMB_SER_NO", requestData.getString("MEMB_SER_NO"));
	    paramMap.put("PERS_COP_TY", requestData.getString("PERS_COP_TY"));
	    paramMap.put("ID_NO", requestData.getString("ID_NO"));
	    paramMap.put("BIR_DAY", requestData.getString("BIR_DAY"));
	    paramMap.put("BUSI_NO", requestData.getString("BUSI_NO"));
	    paramMap.put("MOB_PH_NO", requestData.getString("MOB_PH_NO"));
	    paramMap.put("POST_NO", requestData.getString("POST_NO"));
	    paramMap.put("MEMB_NM", requestData.getString("MEMB_NM"));
	    paramMap.put("PH_NO", requestData.getString("PH_NO"));
	   	paramMap.put("ADDR1", requestData.getString("ADDR1"));
		paramMap.put("ADDR2", requestData.getString("ADDR2"));
		paramMap.put("MEMB_ENG_NM", requestData.getString("MEMB_ENG_NM"));
		paramMap.put("EMAIL", requestData.getString("EMAIL"));
		
	    // 디버깅 코드 추가
	    System.out.println("AddCust.do 요청 받음");
	    System.out.println("전달된 파라미터: " + paramMap);
	    
	    // 서비스에서 클라이언트 추가 메소드 호출하도록 수정
	    int result = posService.insertCust(paramMap);

	    String message;
	    if (result == 0) {
	        // 이미 등록된 경우
	        message = "이미 등록된 회원입니다.";
	    } else {
	        // 클라이언트 등록 성공
	        message = "회원 등록이 성공적으로 완료되었습니다.";
	    }
	    // 응답 데이터셋에 메시지 추가
	    dataRequest.setResponse("message", message);
	    dataRequest.setResponse("result", result);
	    
	    // JSON 형식의 응답을 반환합니다.
	    return new JSONDataView();
	}

	@RequestMapping(value = "/GetProdData.do", method = RequestMethod.POST)
	public View getProdData(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) {
		JSONObject requestData = dataRequest.getRequestObject();

		String prodCls = requestData.optString("prodCls", null); // null이면 null을 반환
	    String searchInput = requestData.getString("searchInput");
	    String clientNm = requestData.getString("clientNm");
		System.out.println("prodCls: " + prodCls);
		
		// Prepare parameters
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("prodCls", prodCls);
		paramMap.put("searchInput", searchInput);
		paramMap.put("clientNm", clientNm);	

		System.out.println("paramMap: " + paramMap);

		// Call service to get sales data
		List<Map<String, Object>> productData = posService.selectProduct(paramMap);

		System.out.println("productData: " + productData);
		// Prepare response
		dataRequest.setResponse("productData", productData);

		return new JSONDataView();
	}
	
	@RequestMapping("/PosProductList.do")
	public UIView productList(HttpServletRequest req, HttpServletResponse res, DataRequest dataReq) throws Exception {
	    
	    // 포워드하여 PosMain.clx로 요청 전달
	    return new UIView("/ui/PosProductList.clx");
	}
}
