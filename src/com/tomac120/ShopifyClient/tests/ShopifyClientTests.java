package com.tomac120.ShopifyClient.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.bulkRenameApp.classes.BulkRenameApp;
import com.tomac120.JavaFunctions.JavaFunctions;
import com.tomac120.ShopifyClient.ShopifyClient;
import com.tomac120.Tests.TestResult;

import java.util.concurrent.ThreadLocalRandom;

public class ShopifyClientTests {
	protected String apiKey = "";
	protected String apiSecret = "";
	protected String testShopDomain = "";
	protected String shopifyToken = "";
	protected String newProductId;
	
	public void doTests(List<TestResult> testsArray) {
		testsArray.add(this.getProducts());
		testsArray.add(this.addNewProduct());
		testsArray.add(this.updateProduct());
		testsArray.add(this.deleteProduct());
	}
	
	protected ShopifyClient getShopifyClient(){
		BulkRenameApp app = BulkRenameApp.getInstance();
		return new ShopifyClient(this.testShopDomain,this.shopifyToken,app.shopifyApiKey,app.shopifyApiSecret);
	}

	protected TestResult getProducts(){
		TestResult r = new TestResult("getProducts");
		
		//
		//$params = Array('limit'=> $limit,'page'=> $i);
		//$products_i = $shopifyClient->call('GET', '/admin/products.json',$params);
		Map<String,String> params = new HashMap<String,String>();
		params.put("limit", "10");
		params.put("page", "1");

		ShopifyClient c = this.getShopifyClient();
		String path = "/admin/products.json";
		Map<String,Object> result = c.get(path, params);
		if (result != null){
			return r.success();
		}
	
		return r;
	}
	

	protected TestResult addNewProduct(){
		TestResult r = new TestResult("addNewProduct");
		
		int random = ThreadLocalRandom.current().nextInt(0, 1000000 + 1);

		Map<String,Object> params = new HashMap<String,Object>();
		Map<String,Object> product = new HashMap<String,Object>();
		params.put("product", product);
		product.put("title", "This is a title of a test product "+Integer.toString(random));
		product.put("body_html", "Test HTML from addNewProduct.");
		
		ShopifyClient c = this.getShopifyClient();
		String path = "/admin/products.json";
		Map<String,Object> result = c.post(path, params);
		if (result != null){
			Map<String,Object> products = JavaFunctions.mapStringObjectFromMap(result, "product");
			if (products != null){
				long id = JavaFunctions.intFromMap(products, "id");
				if (id > 0){
					this.newProductId = String.valueOf(id);
					return r.success();
				} else {
					return r.failure("id not parsed > 0");
				}
			} else {
				return r.failure("products=null");
			}
		}
	
		return r;
	}
	

	protected TestResult deleteProduct(){
		TestResult r = new TestResult("deleteProduct");
		if (newProductId == null){
			return r.wasNotRun();
		}

		Map<String,String> params = new HashMap<String,String>();
		
		ShopifyClient c = this.getShopifyClient();
		String path = "/admin/products/"+this.newProductId+".json";
		Map<String,Object> result = c.delete(path, params);
		if (result != null){
			return r.success();
		}
	
		return r;
	}
	
	protected TestResult updateProduct(){
		TestResult r = new TestResult("updateProduct");
		if (newProductId == null){
			return r.wasNotRun();
		}

		Map<String,Object> params = new HashMap<String,Object>();
		Map<String,Object> product = new HashMap<String,Object>();
		params.put("product", product);
		product.put("id", this.newProductId);
		product.put("body_html", "Test HTML from updateProduct.");
		
		ShopifyClient c = this.getShopifyClient();
		String path = "/admin/products/"+this.newProductId+".json";
		Map<String,Object> result = c.put(path, params);
		if (result != null){
			return r.success();
		}
	
		return r;
	}
}
